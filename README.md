# Database Project

## 프로젝트 설명
조회 성능 개선을 위해 사용되는 방법들인 캐싱 서버 적용, 데이터베이스 샤딩이 실제 조회 성능 개선에 어느 정도 영향을 줄 수 있는지를 테스트하는 프로젝트입니다. 
50만 데이터에 중 하나의 id(인덱스 X)를 탐색하는 조회 API 환경을 만들고 로컬 캐시, 레디스 캐시, 샤딩 데이터베이스 일 때의 도커 컨테이너 환경을 만들어 
기본 환경(캐시 X, 샤딩 X) 대비해서 조회 속도가 어느 정도 개선되었는지 바로 체감할 수 있습니다. 


## 프로젝트 파일 설명
1. `docker-compose.yml`: 대조군을 위한 프로젝트 환경
2. `docker-compose.local.yml`: 로컬 캐시 저장소를 사용한 프로젝트 환경
3. `docker-compose.redis.yml`: 레디스 캐시 저장소를 사용한 프로젝트 환경
4. `docker-compose.shard.yml`: 레디스 캐시 저장소와 샤딩 데이터베이스를 사용한 프로젝트 환경   

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

## 샤딩 데이터베이스 준비에 필요한 
