import java.io.*;
import java.util.StringTokenizer;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;


public class Main {
    static class Code implements Comparable<Code> {
        String url;
        int t;      // 시간
        int p;      // 우선순위

        Code(String url, int t, int p) {
            this.url = url;
            this.t = t;
            this.p = p;
        }

        @Override
        public int compareTo(Code c) {
            if(c.p == this.p) {     // 우선순위가 같으면
                return Integer.compare(this.p, c.p);    // 시간이 빠른 순
            }
            return Integer.compare(this.p, c.p);       // 우선순위가 작은 순
        }
    }

    static class Judge {
        int start;  // 채점을 시작한 시각
        String u;

        Judge(int start, String u) {
            this.start = start;
            this.u = u;
        }
    }

    static class Time {
        int first;      // 시작 시각
        int second;     // 끝나는 시각

        Time(int first, int second) {
            this.first = first;
            this.second = second;
        }
    }

    static List<Code> waitingLst = new ArrayList();    // 채점 대기큐
    static Set<String> requestUrlSet = new HashSet();      // 채점 대기큐에 있는 url
    static Set<String> judgeDomainSet = new HashSet();     // 채점 중인 도메인
    static Map<String, Time> domains = new HashMap();   // 채점이 종료된 도메인 저장
    static Judge[] judges;  // 크기가 N인 채점기
    static int N;       // 채점기 갯수

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
                judges = new Judge[N + 1];  // 채점기 배열 생성
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
        waitingLst.add(new Code(u0, 0, 1));
    }

    /*
        명령어가 200일때 실행하는 채점 요청
        p: 우선순위
        t: 시간
        u: URL
    */
    public static void request(int p, int t, String u) {
        // 채점 대기 큐에 있는 task 중 정확히 u와 일치한다면
        if(requestUrlSet.contains(u)) {
            return;
        }

        requestUrlSet.add(u);       // 채점 대기큐 목록에 url명 추가
        waitingLst.add(new Code(u, t, p));      // 채점 대기큐에 객체 추가
    }

    /*
        명령어가 300일 때 실행되는 채점 시도
        t: 시각
    */
    public static void play(int t) {
        // 쉬고 있는 채점기 찾기
        int judgeMachine = -1;      // 쉬고 있는 채점기 번호
        for(int n=1; n<=N; n++) {       // 채점기 번호
            if(judges[n] == null) {   // 쉬고 있는 채점기라면
                judgeMachine = n;
                break;
            }
        }

        if(judgeMachine == -1) {      // 쉬고 있는 채점기가 없다면
            return;
        }

        // 채점 대기 큐에서 즉시 채점이 가능하고 우선순위가 가장 높은 채점 task 고르기
        String domain = "";
        Collections.sort(waitingLst);       
        for(int i=0; i<waitingLst.size(); i++) {
            Code code = waitingLst.get(i);
            String curDomain = code.url.split("/")[0];  // 현재 도메인

            if(judgeDomainSet.contains(curDomain)) {    // 현재 채점이 진행 중인 도메인이라면
                continue;     // 채점 불가
            } else if(domains.containsKey(curDomain) && !isAvailable(t, curDomain)) {   // 부적절한 채점이라면
                continue;     // 채점 불가
            } else {
                waitingLst.remove(i);    // 채점 대기큐에서 제거
                requestUrlSet.remove(code.url);    // 채점 대기큐 url명 제거
                domain = curDomain;
                judgeDomainSet.add(domain);       // 채점을 진행 중인 도메인으로 저장
                judges[judgeMachine] = new Judge(t, domain);     // 채점 시작
                break;
            }
        }
    }

    /*
        gap = end - start
        채점이 불가능한 조건을 만족하면 false 반환
        t: 시각
        curDomain: 도메인
    */
    public static boolean isAvailable(int t, String curDomain) {
        Time time = domains.get(curDomain);
        int start = time.first;     // 도메인을 채점한 시각
        int gap = time.second - start;  // 도메인의 채점 시작 시각과 종료 시각 차이
        
        if(t < start + 3 * gap) {
            return false;       // 채점 불가
        }
        return true;        // 채점 가능
    }

    /*
        명령어 400일때 채점 종료
        t: 시각
        jId: 채점기 번호
    */
    public static void end(int t, int jId) {
        if(judges[jId] == null) {     // jId번째 채점기에 진행하던 채점이 없다면
            return;
        }

        Judge judge = judges[jId];
        domains.put(judge.u, new Time(judge.start, t));     // 채점이 종료된 도메인은 시작 시각, 종료 시각을 저장
        judgeDomainSet.remove(judge.u);     // 채점 중인 도메인 목록에서 제거
        judges[jId] = null;     // jId번 채점기를 쉬는 상태로 만든다
    }
}