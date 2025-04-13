# 배치 작업
데이터를 실시간으로 처리하는게 아닌 일괄적으로 모아 처리하는 작업

## 종류
- 예약 시간에 광고성 메시지 발송
- 결제 정산 작업
- 운영을 위해 필요한 통계 데이터 구축
- 대량 데이터를 필요로 하는 모델 학습 작업

## 왜 배치인가?
1) 메시지 예약처럼 기능 스펙상으로 실시간으로 처리할 수 없는 경우 정해진 시간에 메시지를 발생해야 하므로  
   사용자가 해당 시간에 대기하다가 클릭하지 않는 이상 발송이 불가능하다.


2) 리소스를 효율적으로 사용할 수 있기 때문  
   통계데이터를 예로 들었을 때 만약 데이터를 요구사항에 따라 주간, 월간으로 수집한 통기 보고서를 받고자 한다면  
   매번 실시간으로 데이터를 갱신해서 보고서를 작성하는 것보다는 해당 주에, 주말 또는 해당 월에, 월말에 모아 처리하는 것이 보다 합리적이다.  


3) 대량의 데이터를 export(저장) 해야되는 기능  
   사용자가 웹서비스 내에서 대량의 데이터를 익스포트 하는 버튼을 통해 100만건이 넘는 데이터를 CSV 형태로 export하고 싶다면  
   데이터를 저장소로부터 읽어와 100만 로우가 넘는 데이터를 쓰고있는(파일로 저장하는) 동안 사용자가 아무런 작업도 할수 없다면 안되기 때문에  
   배치작업을 사용하면 사용자에게 빠른 응답을 준다.  
   시간이 오래걸리는 작업은 백그라운드에서 처리되고 작업이 완료된 이후에 사용자가 그 파일을 다운받을 수 있게 구성할 수 있다.  
  

4) 한정된 서버에서 여러 사람들이 엄청난 데이터를 가지고 모델 학습을 진행하려고 한다.  
   만약 한대의 서버에서 한번에 한개의 작업만 돌릴 수 있고 그 작업이 아주 오래걸린다면?  
   이미 특정 작업이 돌아가고 있다면 다른 사람은 그 작업을 실행하지 못하게 된다.  
   주기적으로 실행중인지 확인하는것은 불편하니, 사용 시간을 예약해 두고 내 예약시간에 이전 작업이 종료되고, 내 작업이 자동으로 실행되도록 한다면  
   서버가 노는 시간 없이 효율적으로 작업을 위해 사용할 수 있다.  

## 왜 스프링 배치인가?
스프링에서 제공하는 특성 그대로 사용 가능.  
특정 환경에 종속되지 않는 JVM의 유연성. 즉, 플랫폼에 관계 없이 서버, 클라이언트, Docker 등 여러 플랫폼을 통해 배포하더라도 각각의 플랫폼마다 개발할 필요 없이 하나의 개발로 가능하며 유지보수 장점도 존재한다.  
스프링 프레임워크의 DI를 통한 객체간 결합을 구성할 수도 있고, AOP로 반복적인 코드를 쉽게 줄일 수 있다.  
만약 DB를 호출하더라도 직접 로우한 부분까지 개발을 하지 않고도 이미 구현된 라이브러리를 활용하면 쉽게 짧은 코드로 완성할 수 있다.  
테스트또한 용이하므로 유지보수시 많은 시간절약이 가능하다.  

굉장히 많은 데이터들에 대한 세세한 수치를 확인할 수 있는데, 해당 수치데이터와 개발하면서 남기는 로깅등을 통해  
이슈 발생시 어떤 부분이 잘못되어 이렇게 처리되었는지를 파악이 가능하고, 데이터를 신뢰성 있게 보정할 수 있다.  

이러한 스프링 배치는 스케줄러와는 다른 개념이다.  
특정 시간, 특정 이벤트에 따라 JOB이 실행되는 스케줄링 기능이 없다.  
프로젝트 내에서 스프링에서 제공하는 스프링 스케줄러, Quartz와 같은 스케줄러들을 추가하여 함께 사용할 수는 있다.  
즉, 스프링 배치는 JOB 관리를 하지만 JOB을 실행시키는 주체는 아니다.  

# Spring Batch 구조(Layer)

- `Application`  
  　↓　　↑
- `Batch Core`  
  　↓　　↑
- `Batch Infrastructure`

**Application** : 배치 처리를 위한 모든 사용자의 코드와 설정이 포함된다.  
**Batch Core** : 배치를 정의하는 JOB, step과 실행에 사용되는 JOB-Launcher, JOB-Parameter 등을 말한다.  
**Batch Infrastructure** : 파일 DB 등에서 데이터를 읽고 쓸 수 있는 item reader, item writer를 포함한다.  

# 이용권 서비스 요구사항
시간단위로 구성할 수 있는 이용권, 예약을 컨텐츠로 구성

# 헬스장 PT 등록
직원은 여러 프로그램을 소개시켜 주었고, 그것들은 각각 유효기간, 횟수가 다르다.
- 2개월-10회 PT 선택 - 오픈 기념 PT등록 3개월 헬스장 이용권 추가 획득
- 예약한 수업 시작 전 알림 수신 / 수업 종료 후 유효기간 자동 1일 차감
- 헬스장 내 필라테스 수업오픈 - 모든 회원에게 1회 이용권 일괄 지급

# 도메인 별 정리

- **이용권**  
  - 사용자는 N개의 이용권 소지 가능.
  - 이용권은 횟수가 모두 소진되거나 이용기간이 지나면 만료.
  - 이용권 만료 전 사용자에게 남은 횟수에 대한 알림.
  - 업체에서 원하는 시간을 설정하여 일괄로 사용자에게 이용권을 지급.

- **수업**  
  - 예약된 수업 10분 전 출석 안내 알람 제공
  - 수업 종료 시점에 수업을 예약한 학생의 잔여 이용권 횟수 일괄 차감 (-1)

사용자의 수업 예약, 출석, 이용권 횟수 등의 데이터로 유의미한 통계데이터 생성

# Feature
- **Batch**  
    - 이용권 만료
    - 이용권 일괄 지급
    - 수업 전 알림
    - 수업 후 이용권 차감
    - 통계 데이터 구축

- **View**  
    - 사용자 이용권 조회 페이지
    - 관리자 이용권 등록 페이지
    - 관리자 통계 조회 페이지

- **API**  
    - 사용자 이용권 조회 API
    - 관리자 이용권 등록 API
    - 관리자 통계 조회 API

# Batch - Step
- **수업 전 알림**  
  알람대상인 사용자를 뽑아오는 Step  
  알을 전송하는 Step  
  배치 처리를 정의하고 제어하는 독립된 작업의 단위를 말한다.  
  순차적으로 이루어진 작업을 캡슐화 한 도메인 객체로 생각하면 된다.  
  Tasklet 기반, Chunk기반 으로 나눌 수 있다.  


- **Tasklet**  
  단일 작업을 처리할 때 사용  
  오래된 데이터삭제, 이미정의된 알람 전송  
  굉장히 간단한 하나의 작업을 표현할 수 있다.  
  아이템을 기반으로 읽기/쓰기 등의 작업들도 Tasklet으로 구현  
  하나의 Tasklet 안에 로직으로 데이터 읽기/쓰기 구성시 Chunk와 동일하게 구현 가능  


- **Chunk**  
  아이템 기반 으로 처리하여 ItemReader, ItemProcessor, ItemWriter 로 구성  
  ItemProcessor는 필수요소가 아니기 때문에 필요한 경우만 사용  
  이들을 하나의 Chunk로 정의할 수 있으며, 3가지의 루프를 수행한다.　
 
    1) ItemReader에서 Chunk단위(한번에 처리할 데이터 개수) 정의하고 정의한 아이템들을 read() 메소드를 통해 반복하여 가져온다.
    2) ItemProcessor를 통해 가져온 Item갯수만큼 procces()를 반복하여 진행하고, process()작업 을 모두 마친 item들을 한번에
       ItemWriter에 전달하여 작업을 진행하게 된다.
    3) 루프는 이러한 chunk가 읽을 아이템이 없을 때 까지 전체를 반복한다.
       chunk단위로 트랜잭션이 있기 때문에 chunk단위로 Commit Rollback이 이루어진다.


# Batch - Job
처음부터 끝까지 독립적으로 실행할 수 있으며, 고유하고 순서가 지정된 여러 스텝들의 모음이다.  
유일하고, 고유하고 순서를 가진 여러 스탭들의 모음으로 외부 의존성에 영향을 받지 않고 실행이 가능해야 하는 독립적인 작업이다.  
정의한 스텝의 순서대로 작업을 진행한다.  

JobRepository는 배치 수행과 관련된 작업을 가지고 있다.  
시작/종료 시간, 읽기/쓰기 횟수, Job의 상태 등을 모두 관리하게 된다.  
일반적으로 rdb를 사용한다.  
스프링 배치 내의 대부분의 컴포넌트들이 배치수행과 관련된 데이터를 공유한다.  
Job Launcher는 Job의 실행을 담당하며, 이외에도 현재 스레드에서 수행할지, 스레드풀을 이용할지 Job실행에 필요한 파라미터의 유효성 등에 대한 작업도 함께 실행한다.  
Job Launcher를 통해 Job을 실행하게 되면 Chunk기반으로 정의된 Step을 수행하고, JobRepository에 업데이트한다.  

# JobRepository TABLE
기본적으로 제공되는 RDB를 통해 생성되는 6개의 테이블로 이루어져 있다.  
SPRING 설정을 통해 런타임시 TABLE이 없으면 자동으로 생성되도록 설정 가능하다.  

- **BATCH_JOB_INSTANCE**  
  JOB을 처음 실행하게 되면 단일 JOB INSTANCE 데이터가 저장된다.  
- **BATCH_JOB_EXCUTION**  
  JOB의 실제 실행 기록을 나타낸다.  
  JOB이 실행될 때 마다 새로운 데이터가 생성되고, 실행되는 동안에도 주기적으로 상태값 등이 업데이트 된다.

    - **BATCH_JOB_EXCUTION_CONTEXT**  
      해당 테이블 데이터를 통해 재시작과 같은 배치를 여러번 수행하는 작업에서 유용하게 사용할 수 있다.
    - **BATCH_JOB_EXCUTION_PARAMS**  
      JOB이 매번 실행할 때마다 사용된 JOB PARAMETER를 기록한다.
    - **BATCH_STEP_EXCUTION**  
      STEP의 시작/완료 등의 상태와 읽기/쓰기/건너뛰기 등의 데이터들을 저장한다.

      - **BATCH_STEP_EXCUTION_CONTEXT**  
        BATCH_JOB과 동일하게 EXCUTION_CONTEXT라는 개념이 있으며, 동일한 용도로 사용한다.

기본적으로 RDB에서 구성되지만 개발환경이나 테스트환경에서 사용할 수 있는 INMEMORY방식도 존재한다.  
외부 db가 당장 구성되지 않은 상태에서 구성하는데 시간을 사용하는것 보다 H2데이터베이스 등을 적용하여 사용하는 등  
INMEMORY DB 방식을 사용하게 되면 RESOURCE를 줄일 수 있는 좋은 방법이 될 수 있다.


# JOB feature

## 이용권 만료 Job
1개의 Chunk Step으로 구성

- **ExpirePassesReader**  
  이용권 만료 대상을 읽어들인다.
- **ExpirePassesWriter**  
  만료 상태로 업데이트 시킨다.

## 이용권 일괄 지급 Job
1개의 Tasklet Step으로 구성

- **AddPassesTasklet**  
  업체에서 여러명의 회원에게 이용권을 일괄로 지급  
  Admin에 등록후 정해진 시간에 이용권이 사용자들에게 지급된다.  
## 예약 수업 전 알람 Job
기업이 사업이 잘되면 데이터 양은 자연스럽게 증가하게 되며, 더 많은 고객이 더 많은 접근을 하여  
더 많은 데이터가 쌓이더라도 배치작업은 정상적으로 동작되야 한다.  
따라서 확장할 수 있는 방식으로 복수개의 Thread Chunk로 병렬처리 한다.  

2개의 Step으로 구성
- **AlarmReader**  
  알람 대상 가져오기
- **AlarmWriter**  
  알람 전송하기

chunk기반의 step에서는 chunk단위로 적용되고 각자 독립적인 transaction이 적용된다.
만건의 데이터가 있다고 가정했을 때 commit수를 100개로 설정했다면, 100개의 chunk가 실행-종료 되는것이 100번 반복하게된다.
뒤에있는 작업이 시작하기 까지 많은 시간이 소요된다.
따라서 이 작업들을 병렬로 처리하게 되면 100개씩 처리할때 3개로 나누었을 때 한번에 300개의 작업을 치룰수 있다.
이때, 멀티스레드에 대한 주의점이 있으며 스프링 배치 뿐만 아니라 다른 모든 프로그래밍에서 멀티스레드를 사용하게되면 주의해야한다.

## 수업 종료후 이용권 차감 JOB
1개의 비동기 Chunk Step으로 구성
- **UsePassesReader** 
- **AsyncItemProcessor**
- **AsyncItemReader**

특정 Process작업이 굉장히 복잡한 수학계산, 매우 많은 외부 서비스 호출 등에 의해 시간이 매우 오래 소요되는 프로세스가 있을 수 있다.  
이런 경우 비동기로 사용하게 되면 각 호출에 대해 future(Promise) 반환하여 AsyncWriter에 전달한다.  
이후 AsyncWriter에게 최종 결과값을 넘겨주고 실행을 위임한다.  

## 통계 데이터 생성
2개의 Chunk step으로 구성되며, Chunk step 종료 후 다시 2개의 병렬 Tasklet step으로 구성  
시간당으로 쌓아 올린 이용권, 예약 수업 등의 데이터를 통해 예약, 횟수등을 통계자료로 만들어 놨다고 가정한다.  
이러한 작업을 1개의 Step에서 처리하고, 해당 데이터를 읽어들여 보고서를 만드는 작업에 대한 Step으로 각각 구성한다.  

### Chunk step
- **StaticsReader**  
  시간당 통계 데이터
- **StaticsWriter**  
  Export Reports (보고서 저장)

### Tasklet step(Async)
병렬로 구성 (서로 관련이 없는 작업을 수행할 때 사용하는 방식)

- **DayStaticsTasklet**  
  일별 보고서 파일 생성
- **DayStaticsTasklet**  
  주간별 보고서 파일 생성

시간별 통계 자료가 있었을 때, 일별 보고서 파일을 만드는 작업과, 주간별 보고서 파일을 만드는 작업은 서로 다른 작업이며,
이를 병렬로 동시에 수행하여 처리량을 높일 수 있다.

## Git Flow 전략
`Master` → `Develop` → `feature/do-something`

# Application 구동 Batch 테스트
```java
@EnableBatchProcessing
@SpringBootApplication
public class PassBatchApplication {
    /* 생략 */
}
```
`@EnableBatchProcessing` 애노테이션을 추가한다.  
해당 애노테이션을 통해 스프링 배치가 작동된다.  
스프링 배치의 모든 초기화 및 실행을 이루고 총 4개의 설정 클래스를 실행시킨다.
스프링 부트 배치의 자동 설정 클래스가 실행되므로 빈으로 등록된 JOB을 초기화해서 초기화와 동시에 JOB을 수행하도록 구성되어 있다.

JOB을 만들기 위해서는 Step을 만든 후 Step을 기반으로 Job을 구성해야 하므로  
다음으로는 JOB과 STEP을 위한 의존성 주입을 하도록 한다.
```java
private final JobBuilderFactory jobBuilderFactory;
private final StepBuilderFactory stepBuilderFactory;

public PassBatchApplication(JobBuilderFactory jobBuilderFactory, StepBuilderFactory stepBuilderFactory) {
    this.jobBuilderFactory = jobBuilderFactory;
    this.stepBuilderFactory = stepBuilderFactory;
}
```

#### JobBuilderFactory Bean 주입 에러
```text/plain
Could not autowire. No beans of 'JobBuilderFactory' type found
```
의존성 주입시 위와 같은 오류 발생시 Gradle Refresh를 한 후 우측 하단에 아래와 같은 IDE 알림창에서 추가 설정을 해주면 해결된다.
```text/plain
suggested plugins jakarta EE: Batch Applications, Spring Batch avaliable for dependencies
```

### Step Bean 등록
```java
@Bean
public Step passStep() {
    return this.stepBuilderFactory.get("passStep") // Step의 이름을 선언한다.
            .tasklet((contribution, chunkContext) -> { //Tasklet 방식으로 Tasklet 인터페이스의 execute() 메소드를 람다로 구현
                System.out.println("Execute PassStep");
                System.out.println("contribution = " + contribution + ", chunkContext = " + chunkContext);
                return RepeatStatus.FINISHED; // 종료
            }).build();
}
```
### Job Bean 등록
```java
	@Bean
	public Job passJob() {
		return this.jobBuilderFactory.get("passJob")
				.start(passStep())
				.build();
	}
```

# Docker 설정

- `./docker-compose.yml` 파일

    ```yaml
    version: '3.8' # Docker Compose 파일의 버전
    
    services:
      mysql:
        container_name: mysql_local
        image: mysql:8.0.30
        volumes: # Docker 컨테이너가 삭제될때 데이터도 함께 삭제된다. 이러한 생명주기와 상관없이 데이터를 유지할 수 있도록 하는 방법
          - ./db/:/etc/mysql/conf.d
          # host 디렉토리:컨테이너 - host가 그대로 컨테이너를 생성하며 연결된다.
          # 해당 정보를 가지고 있으면 컨테이너를 내렸다 올려도 호스트에 정의한 데이터가 변함없이 들어가게 된다.
          # 호스트에서 설정 파일과 초기실행할 쿼리들을 관리하기 위함이다.
          # MySQL서버를 설치하게 되면 기본적으로 my.cnf라는 이름의 설정파일로 따르게 되며,
          # conf.d 디렉토리 하위의 파일에 설정하고 싶은 커스텀 설정 파일을 넣어주면 된다.
          - ./db/initdb.d:/docker-entrypoint-initdb.d
          # 컨테이너가 시작되면 해당 디렉토리에 존재하는 sh, sql file을 실행한다.
          # sql파일에 create table문이 추가되면 테이블이 생성되고, insert문으로는 초기 데이터 설정도 가능하다.
          # 주의점은 파일 명에 따라 알파벳 순으로 실행되므로 먼저 create가 되도록 설정해야 한다.
        ports:
          - "3306:3306"
        environment:
          - MYSQL_DATABASE=pass_local
          - MYSQL_USER=pass_local_user
          - MYSQL_PASSWORD=passlocal123
          - MYSQL_ROOT_PASSWORD=passlocal123
          - TZ=Asia/Seoul
    
    ```

- `.db/conf.d/custom.cnf` 파일
    ```text/plain
    [client]
    default-character-set = utf8mb4
    
    [mysqld]
    authentication-policy = mysql_native_password
    ```

- `.db/initdb.d/create-table.sql` `.db/initdb.d/insert_data.sql` 등 sql파일 추가

- `./Makefile` 파일
Docker Compose를 실행
    ```makefile
    #백그라운드 실행, 강제 재생성
    db-up:
        docker-compose up -d --force-recreate
    
    # volume 삭제
    db-down:
        docker-compose down -v
    ```
telminal 에서 Makefile에 지정한  실행한다.

```ba
make db-up
```

### Window OS - make 명령
Window에서는 위 명령을 실행할 수 없다.  
https://kjs92980.github.io/p/use-make-in-windows/  
위 링크를 통해 Chocolatey를 설치하여 make명령어를 실행한다.  
주의할점은 반드시 PowerShall을 `관리자 권한`으로 실행해야 한다.

### DBeaver 한글 깨지는 경우

```dockerfile
FROM mysql:8.0.30
COPY ./db/conf.d /etc/mysql/conf.d
COPY ./db/initdb.d /docker-entrypoint-initdb.d

RUN chmod 644 /etc/mysql/conf.d/custom.cnf
```

```yaml
version: '3.8' # Docker Compose 파일의 버전

services:
  mysql:
    build: .
    ports:
      - "3306:3306"
    environment:
      - MYSQL_DATABASE=pass_local
      - MYSQL_USER=pass_local_user
      - MYSQL_PASSWORD=passlocal123
      - MYSQL_ROOT_PASSWORD=passlocal123
      - TZ=Asia/Seoul
```

```makefile
#백그라운드 실행, 강제 재생성
db-up:
	docker-compose up -d --build --force-recreate

# volume 삭제
db-down:
	docker-compose down -v
```

위와같이 설정하면 더이상 한글이 깨지지 않는다.