import java.io.*;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.PriorityQueue;
import java.util.Arrays;


public class Main {
    static class Code implements Comparable<Code> {
        int id;     // 인덱스
        int no;    // 문제 번호
        int t;      // 시간
        int p;      // 우선순위
        String domain;  // 도메인

        Code(int id, int t, int p, String domain, int no) {
            this.id = id;
            this.t = t;
            this.p = p;
            this.domain = domain;
            this.no = no;
        }

        @Override
        public int compareTo(Code c) {
            if(c.p == this.p) {     // 우선순위가 같으면
                return Integer.compare(this.p, c.p);    // 시간이 빠른 순
            }
            return Integer.compare(this.p, c.p);       // 우선순위가 작은 순
        }
    }

    final static int MAX_D = 300;       // 도메인의 최대 갯수
    static final int MAX_N = 50000;     // 채점기의 최대 갯수

    static Set[] isInReadyq = new HashSet[MAX_D + 1];   // 인덱스 = 도메인의 인덱스, 값 = 문제 ID 집합
    static List<Code> waitingLst = new ArrayList();    // 채점 대기큐
    static Map<String, Integer> domainToIdx = new HashMap();    // 도메인 : 인덱스
    static PriorityQueue<Integer> ableJudgeMachine = new PriorityQueue();       // 쉬고 있는 채점기
    static int[] judgingDomain = new int[MAX_N + 1];        // 값 = 채점하는 도메인 인덱스
    static int N;       // 채점기 갯수
    static int[] s = new int[MAX_D + 1];        // 도메인 별 채점 시작 시각
    static int[] g = new int[MAX_D + 1];        // 도메인 별 시작 시각과 종료 시각 차이
    static int[] e = new int[MAX_D + 1];        // 도메인 별 s[i] + 3 × g[i]
    static int idx;     // 도메인 인덱스

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int Q = Integer.parseInt(st.nextToken());        // 명령의 수
        
        // 명령의 정보 입력 받기
        for(int q=0; q<Q; q++) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());     // 명령어

            if(cmd == 100) {        // 코드트리 채점기 준비
                N = Integer.parseInt(st.nextToken());        // 채점기 갯수
                String u0 = st.nextToken();      // 초기 문제 URL
                
                prepare(u0);
            } else if(cmd == 200) {     // 채점 요청    
                int t = Integer.parseInt(st.nextToken());   // 초
                int p = Integer.parseInt(st.nextToken());   // 우선순위
                String u = st.nextToken();   // URL

                request(p, t, u);
            } else if(cmd == 300) {     // 채점 시도
                int t = Integer.parseInt(st.nextToken());   // 초

                play(t);
            } else if(cmd == 400) {        // 채점 종료
                int t = Integer.parseInt(st.nextToken());       // 초
                int jId = Integer.parseInt(st.nextToken());     // 채점기 번호

                end(t, jId);
            } else if(cmd == 500) {     // 채점 대기 큐 조회
                int t = Integer.parseInt(st.nextToken());   // 시간
                bw.write(waitingLst.size() + "\n");
            }
        }
        bw.flush();
        br.close();
        bw.close();
    }

    /*
        명령어가 100일때 실행
    */
    public static void prepare(String u0) {
        String[] urlArr = u0.split("/");
        String domain = urlArr[0];      // 도메인
        int id = Integer.parseInt(urlArr[1]);   // 문제 ID

        // 쉬고있는 채점기 생성
        for(int i=1; i<=N; i++) {
            ableJudgeMachine.add(i);
        }

        // 대기열 초기화
        for(int i=1; i<=MAX_D; i++) {
            isInReadyq[i] = new HashSet();
        }
        
        if(!domainToIdx.containsKey(domain)) {      // 처음 나온 도메인이라면
            idx++;      // 인덱스
            domainToIdx.put(domain, idx);   // 도메인 : 도메인 인덱스
        }

        int domainIdx = domainToIdx.get(domain);        // 도메인의 인덱스
        isInReadyq[domainIdx].add(id);      // 도메인의 인덱스 값에 문제 ID를 저장
        waitingLst.add(new Code(domainIdx, 0, 1, domain, id));
    }

    /*
        명령어가 200일때 실행하는 채점 요청
        p: 우선순위
        t: 시간
        u: URL
    */
    public static void request(int p, int t, String u) {
        String[] url = u.split("/");
        String domain = url[0];     // 도메인
        int id = Integer.parseInt(url[1]);      // 문제 번호

        if(!domainToIdx.containsKey(domain)) {      // 처음 나온 도메인이라면
            idx++;
            domainToIdx.put(domain, idx);
        }
        int domainIdx = domainToIdx.get(domain);    // 도메인 인덱스

        if(isInReadyq[domainIdx].contains(id)) {    // 이미 대기열에 있다면
            return;
        }

        isInReadyq[domainIdx].add(id);      // 도메인 인덱스 값으로 문제 번호 추가
        waitingLst.add(new Code(domainIdx, t, p, domain, id));      // 채점 대기큐에 객체 추가
    }

    /*
        명령어가 300일 때 실행되는 채점 시도
        t: 시각
    */
    public static void play(int t) {
        if(ableJudgeMachine.isEmpty()) {      // 쉬고 있는 채점기가 없다면
            return;
        }

        // 채점 대기 큐에서 즉시 채점이 가능하고 우선순위가 가장 높은 채점 task 고르기
        String domain = "";
        Collections.sort(waitingLst);       
        int minDomain = 0;      // 도메인 인덱스
        int lstIdx = -1;        // 리스트에서 우선순위가 높은 task의 인덱스
        Code minCode = new Code(Integer.MAX_VALUE, 0, 0, "", 0);
        for(int i=0; i<waitingLst.size(); i++) {  
            int curDomainId = domainToIdx.get(waitingLst.get(i).domain);  
            if(e[curDomainId] > t) {  // 현재 채점중이거나 현재 시간에 이용할 수 없다면
                continue;
            } else {        // 채점 가능
                minDomain = waitingLst.get(i).id;       // 도메인 인덱스 갱신
                minCode = waitingLst.get(i);
                lstIdx = i;
                break;
            }
        }

        // 우선순위가 가장 높은 url이 있으면 쉬고 있는 가장 낮은 번호의 채점기랑 연결
        if(minDomain > 0) {
            int judgerIdx = ableJudgeMachine.peek();
            ableJudgeMachine.poll();        

            waitingLst.remove(lstIdx);      // 가장 우선순위가 높은 url 지우기

            s[minDomain] = t;       // 채점 시작 시각 할당
            e[minDomain] = Integer.MAX_VALUE;   // 채점 종료 시각 할당(아직 언제 끝나는지 모름)

            judgingDomain[judgerIdx] = minDomain;       // 채점하고 있는 도메인 번호를 갱신
            isInReadyq[minDomain].remove(minCode.no);   // 대기열에서 해당 url의 문제 번호를 지우기
        }
    }

    /*
        명령어 400일때 채점 종료
        t: 시각
        jId: 채점기 번호
    */
    public static void end(int t, int jId) {
        if(judgingDomain[jId] == 0) {     // jId번째 채점기에 진행하던 채점이 없다면
            return;
        }

        ableJudgeMachine.add(jId);      // jId번 채점기는 쉬는 상태로 변함
        int domainIdx = judgingDomain[jId];
        judgingDomain[jId] = 0;     // jId번 채점기가 채점하던 작업 초기화

        g[domainIdx] = t - s[domainIdx];
        e[domainIdx] = s[domainIdx] + 3 * g[domainIdx];
    }
}