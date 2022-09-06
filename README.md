# database_project
조회 성능 개선을 위해 사용되는 방법들인 캐싱 서버 적용, 데이터베이스 샤딩이 실제 조회 성능 개선에 어느 정도 영향을 줄 수 있는지를 테스트하는 프로젝트입니다. 
조회 성능 측정에 사용한 모의 데이터는 [모카루](https://www.mockaroo.com)에서 생성했습니다.

---
## 프로젝트 설명
1. `docker-compose.default.yml`: 비교군을 위한 기본 환경
2. `docker-compose.local_cache.yml`: 로컬 캐시 저장소를 사용한 환경
3. `docker-compose.redis_cache.yml`: 레디스 캐시 저장소를 사용한 환경
4. `docker-compose.shard.yml`: 샤딩 데이터베이스를 사용한 환경   
5. `docker-compose.cache_shard.yml`: 캐싱과 샤딩 데이터베이스를 사용한 환경

---
## 실행 방법
```
docker-compose -f ./docker-compose.default.yml up --build -d
```
