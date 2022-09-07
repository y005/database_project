# Database Project
조회 성능 개선을 위해 사용되는 방법들인 캐싱 서버 적용, 데이터베이스 샤딩이 실제 조회 성능 개선에 어느 정도 영향을 줄 수 있는지를 테스트하는 프로젝트입니다. 
조회 성능 측정에 사용한 데이터베이스에는 50만 개의 더미 데이터가 들어있습니다.

## 프로젝트 설명
1. `docker-compose.local.yml`: 로컬 캐시 저장소를 사용한 프로젝트 환경
2. `docker-compose.redis.yml`: 레디스 캐시 저장소를 사용한 프로젝트 환경
3. `docker-compose.shard.yml`: 레디스 캐시 저장소와 샤딩 데이터베이스를 사용한 프로젝트 환경   

## 데이터베이스 준비
```mysql
create table test
(
    id      int auto_increment
        primary key,
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

## 실행 방법
```
docker-compose -f ./docker-compose.default.yml up --build -d
```
