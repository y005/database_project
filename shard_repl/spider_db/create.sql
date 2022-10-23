-- 테스트 테이블은 데이터베이스에 직접 접속해서 세팅해야 한다.
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