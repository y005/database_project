# Database Project

## 프로젝트 설명
조회 성능 개선을 위해 사용되는 방법들인 캐싱 서버 적용, 데이터베이스 샤딩이 실제 조회 성능 개선에 어느 정도 영향을 줄 수 있는지를 테스트하는 프로젝트입니다. 
50만 데이터에 중 하나의 id(인덱스 X)를 탐색하는 조회 API 환경을 만들고 로컬 캐시, 레디스 캐시, 샤딩 데이터베이스 일 때의 도커 컨테이너 환경을 만들어 
기본 환경(캐시 X, 샤딩 X) 대비해서 조회 속도가 어느 정도 개선되었는지 바로 체감할 수 있습니다. 


## 프로젝트 파일 설명
1. `docker-compose.yml`: 대조군을 위한 프로젝트 환경
2. `docker-compose.local.yml`: 로컬 캐시 저장소를 사용한 프로젝트 환경
3. `docker-compose.redis.yml`: 레디스 캐시 저장소를 사용한 프로젝트 환경
4. `docker-compose.shard_repl.yml`: 레디스 캐시 저장소와 레플+샤딩 데이터베이스를 사용한 프로젝트 환경   

## 기본 데이터베이스 준비에 필요한 설정
```mysql
create table test
(
    id int auto_increment primary key,
    user_id varchar(50) null
);

DELIMITER $$
DROP PROCEDURE IF EXISTS insertRandomData$$

CREATE PROCEDURE insertRandomData()
BEGIN
    DECLARE i INT DEFAULT 1;
    WHILE i <= 500000 DO
        SELECT LEFT(UUID(), 8) INTO @user_id;
        INSERT INTO `database`.test(user_id)
				VALUES (@user_id);
        SET i = i + 1;
    END WHILE;
END$$
DELIMITER $$

CALL insertRandomData;
$$
```

## 기본 환경의 조회 API
### 실행방법
```bash
docker-compose -f ./docker-compose.yml up --build -d
```
### 응답
### 일반 데이터베이스가 사용된 응답
```json
{
  "status": "OK",
  "data": {
    "id": 500000,
    "user_id": "0a916d8b"
  },
  "time": 1113,
  "now": "2022-09-09T03:06:07.010807"
}
```
### 레플 + 샤딩 데이터베이스가 사용된 응답
```json
{
   "status": "OK",
   "data": {
      "id": 500005,
      "user_id": "2daa2778"
   },
   "time": 11,
   "now": "2022-09-09T11:37:02.08804"
}
```

## 로컬 캐시 사용 환경의 조회 API
### 실행방법
```bash
docker-compose -f ./docker-compose.local.yml up --build -d
```
### 캐시가 사용되지 않은 응답
```json
{
  "status": "OK",
  "data": {
    "id": 500000,
    "user_id": "0a916d8b"
  },
  "time": 1164,
  "now": "2022-09-09T02:58:05.842528"
}
```
### 캐시가 사용된 응답
```json
{
  "status": "OK",
  "data": {
    "id": 500000,
    "user_id": "0a916d8b"
  },
  "time": 0,
  "now": "2022-09-09T02:58:46.18094"
}
```

## 레디스 캐시 사용 환경의 조회 API
### 실행방법
```bash
docker-compose -f ./docker-compose.redis.yml up --build -d
```
### 캐시가 사용되지 않은 응답
```json
{
  "status": "OK",
  "data": {
    "id": 500000,
    "user_id": "0a916d8b"
  },
  "time": 3324,
  "now": "2022-09-09T03:14:10.053805"
}
```
### 캐시가 사용된 응답
```json
{
  "status": "OK",
  "data": {
    "id": 500000,
    "user_id": "0a916d8b"
  },
  "time": 8,
  "now": "2022-09-09T03:15:07.522096"
}
```

## 레플 + 샤딩 데이터베이스 환경 구축 과정

1. 샤딩 데이터베이스를 관리할 수 있는 스파이더 엔진을 maria db에 설치한다.
   ```bash
   docker exec -it spider_db bash
   mysql -u root -p < /usr/share/mysql/install_spider.sql
   ```
3. 분산 저장할 데이터베이스 서버 접속과 권한 정보를 등록한다.
    ```sql
    CREATE SERVER master_db1
    FOREIGN DATA WRAPPER mysql
    OPTIONS(
        HOST '172.19.0.3', <-- 마스터 db 1의 ip 주소
        DATABASE 'database',
        USER 'user',
        PASSWORD '1234',
        PORT 3306
    );
    
   CREATE SERVER master_db2
    FOREIGN DATA WRAPPER mysql
    OPTIONS(
        HOST '172.19.0.5', <-- 마스터 db 2의 ip 주소
        DATABASE 'database',
        USER 'user',
        PASSWORD '1234',
        PORT 3306
    );
   ```
4. 테이블을 생성한다.
   ```sql
   create table test(
       id  int auto_increment primary key,
       user_id varchar(50) null
   )
   engine=spider
   comment='wrapper "mysql", table "test"'
   partition by key(id) (
    partition sample1 comment = 'srv "master_db1"', 
    partition sample2 comment = 'srv "master_db2"' 
   );
   ```
5. 파티션 데이터베이스에서 샤딩용 권한과 레플리카용 권한을 생성하고 동일한 스키마의 테이블을 생성한다.
   ```sql
   CREATE USER 'samho101'@'%' IDENTIFIED BY '1234';
   GRANT REPLICATION SLAVE ON *.* TO 'samho101'@'%';
   flush privileges; <--레플리카에 사용하는 계정
   
   create user 'user'@'%' identified by '1234';
   grant all on *.* to 'user'@'%' with grant option;
   flush privileges; <--샤딩에 사용하는 계정
   
   select * from mysql.user;
   show master status;
   use `database`;
   create table test
   (
    id int auto_increment
    primary key,
    user_id varchar(50) null
   ) engine=innodb;
   ```
6. 레플리카를 적용할 슬레이브 데이터베이스에 마스터 데이터베이스 접속 정보와 로그 파일 정보를 설정한 후 구동한다.
   ```sql
   CHANGE MASTER TO
   MASTER_HOST='172.19.0.3',
   MASTER_USER='samho101', <- 레플리카 계정
   MASTER_PASSWORD='1234',
   MASTER_LOG_FILE='mysql-bin.000006', <- 마스터 db status
   MASTER_LOG_POS=2506;
   
   start slave;
   ```
   
## 레플 + 샤딩 데이터베이스와 캐싱 사용 환경의 조회 API
### 설치 방법
1. docker-compose.shar_repl.yml의 컨테이너를 다음과 같은 순서로 실행한다.
   - master_db1, master_db2 (파티션 데이터베이스 컨테이너)
   - slave_db1, slave_db2 (슬레이브 데이터베이스 컨테이너)
   - spider_db (샤딩 관리 데이터베이스 컨테이너)
   - redis1 (캐시 컨테이너)
   - spring1 (스프링 컨테이너)
2. spider_db에 접속해 테이블을 생성한다.
### 응답
### 캐시가 사용되지 않은 응답
```json
{
   "status": "OK",
   "data": {
      "id": 500005,
      "user_id": "2daa2778"
   },
   "time": 228,
   "now": "2022-09-09T11:27:54.547548"
}
```
### 캐시가 사용된 응답
```json
{
   "status": "OK",
   "data": {
      "id": 500005,
      "user_id": "2daa2778"
   },
   "time": 5,
   "now": "2022-09-09T11:30:57.085486"
}
```
