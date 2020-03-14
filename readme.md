## Solr 예제 ##
본 샘플은  검색엔진 Solr용 예제로,

Spring 4 + MyBatis 3 + MariaDB (Maven) 기반으로  제작한 웹 프로젝트 [Project9](https://github.com/gujc71/project9)에 검색 기능을 추가하여 제작하였다.

좀더 자세한 설명은 [여기](https://forest71.tistory.com/208)에서 얻을 수 있다.



### 주요 구현 기능 ###
- 게시판 데이터 수집: 게시글, 댓글, 첨부 파일(Tika)
- 웹 프로젝트에서 통합 검색

### 비슷한 예제  ###
- [Elasticsearch 기반 게시판 예제](https://github.com/gujc71/Project9_es)


### 개발 환경 ###
    Search Engine - Solr 8.4.0
    Programming Language - Java 1.8
    IDE - Eclipse
    DB - MariaDB 
    Framework - MyBatis, Spring 4
    Build Tool - Maven

### Solr 설치 ###
- 설치: [Solr](https://lucene.apache.org/solr/downloads.html) 다운로드 후 압축 해제  
- 실행: solr start
- 코어생성: solr create -c project9
- 재실행: solr restart -p 8983
- JDBC 파일 복사: copy mariadb-java-client-x.x.x to /solr/dist
- 형태소 분석기 설치: copy arirang.lucene-analyzer-x.x.x.jar to /server/solr-webapp/webapp/WEB-INF/lib
- 형태소 분석기 설치: copy arirang-morph-x.x.x.jar to /server/solr-webapp/webapp/WEB-INF/lib
- 스키마 등: copy config files in project9_solr/solr (managed-schema, db-data-config.xml, solrconfig.xml and etc)
- 재실행: solr restart -p 8983
- 풀색인: http://localhost:8983/solr/#/project9/dataimport//dataimport

### Project9 설치 ###
- MariaDB에 데이터 베이스(project9)를 생성하고 tables.sql, tableData.sql를 실행하여 테이블과 데이터를 생성한다.
- applicationContext.xml에 적절한 접속 정보를 입력한다.
- 톰캣이나 이클립스에서 project9를 실행
- http://localhost:8080/project9/ 로 접속
- ID/PW: admin/admin, user1/user1, user2/user2 ...

### License ###
MIT
  
  