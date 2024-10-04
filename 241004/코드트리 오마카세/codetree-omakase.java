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
    static class Query {
        int cmd;    // 명령
        int t;      // 시각
        int x;      // 위치
        String name;    // 이름
        int n;      // 먹을 초밥 갯수

        Query(int cmd, int t, int x, String name, int n) {
            this.cmd = cmd;
            this.t = t;
            this.name = name;
            this.n = n;
        }
    }

    static class Sushi {
        int t;      // 시각
        int x;      // 위치

        Sushi(int t, int x) {
            this.t = t;
            this.x = x;
        }
    }

    static class Person {
        int t;      // 시각
        int x;      // 위치
        String name;        // 이름
        int n;      // 먹을 초밥의 갯수

        Person(int t, int x, String name, int n) {
            this.t = t;
            this.x = x;
            this.name = name;
            this.n = n;
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        int l = Integer.parseInt(st.nextToken());   // 3 <= 초밥 벨트의 길이 <= 1,000,000,000
        int q = Integer.parseInt(st.nextToken());   // 1 <= 명령의 수 <= 100,000
        List<Query> queries = new ArrayList();      // 입력으로 주어진 명령을 담은 리스트
        Set<String> names = new HashSet();      // 사람 이름을 담은 집합
        Map<String, List<Sushi>> sushiMap = new HashMap();     // 초밥별 만들어진 시각 및 위치
        Map<String, Person> personMap = new HashMap();      // 사람별 시각, 위치, 이름, 먹을 초밥의 갯수
        int cntPeople = 0;      // 남아있는 사람 수
        int cntSushi = 0;       // 남아있는 초밥의 수

        // 명령의 정보 입력받기
        for(int i=0; i<q; i++) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());     // 명령어
            int t = -1;     // 시각
            int x = -1;     // 위치
            int n = -1;     // 먹은 초밥 갯수
            String name = "";       // 이름
            if(cmd == 100) {        // 주방장의 초밥 만들기
                t = Integer.parseInt(st.nextToken());       // 1 <= 시각 <= 1,000,000,000
                x = Integer.parseInt(st.nextToken());       // 0 <= 위치 <= L-1
                name = st.nextToken();       // 1 <= 회전 초밥 이름 <= 30
                sushiMap.computeIfAbsent(name, z -> new ArrayList()).add(new Sushi(t, x));  // 처음 만들어진 스시라면 리스트 생성 후 추가
            } else if(cmd == 200) {     // 손님 입장
                t = Integer.parseInt(st.nextToken());       // 1 <= 시각 <= 1,000,000,000
                x = Integer.parseInt(st.nextToken());       // 0 <= 위치 <= L-1
                name = st.nextToken();       // 회전 초밥 이름
                n = Integer.parseInt(st.nextToken());       // 먹을 초밥 갯수
                names.add(name);        // 입장한 사람 추가
                personMap.put(name, new Person(t, x, name, n));     // 입장한 사람의 시각, 위치, 이름, 먹을 초밥 갯수 저장
            } else {    // 사진 촬영
                t = Integer.parseInt(st.nextToken());       // 1 <= 시각 <= 1,000,000,000
            }
            queries.add(new Query(cmd, t, x, name, n));     // 쿼리 추가
        }

        // 사람마다 초밥을 다 먹고 떠나는 시각 구하기
        for(String name : names) {      // 사람 이름
            Person person = personMap.get(name);
            int lastTime = 0;       // 초밥을 다 먹고 떠나는 시각
            
            for(Sushi sushi : sushiMap.get(name)) {     // 초밥
                int eatSushiTime = 0;   // 이 초밥을 먹는 시각
                
                if(person.t > sushi.t) {      // 초밥이 이 사람이 오기 전부터 있었다면
                    int curSushiPos = (sushi.x + (person.t - sushi.t)) % l;          // 이 사람이 들어온 시각에 초밥의 위치
                    int newP = (person.x - curSushiPos + l) % l;        // 초밥과 사람이 만나는 위치
                    eatSushiTime = person.t + newP;     // 이 사람이 온 시각 + 초밥과 사람이 만나는데 걸리는 시간
                } else {    // 초밥이 이 사람이 오자마자 나오거나 그뒤에 나왔다면
                    eatSushiTime = sushi.t + (person.x - sushi.x + l) % l;     // 초밥이 나온 시각 + 사람과 초밥이 만나는데 걸리는 시간
                }

                // 초밥을 먹으면 쿼리에 추가
                queries.add(new Query(111, eatSushiTime, -1, name, -1));
                lastTime = Math.max(lastTime, eatSushiTime);    // 이 사람이 초밥을 다 먹은 시각 갱신    
            }

            // 초밥을 다 먹은 후 떠나는 쿼리를 추가
            queries.add(new Query(222, lastTime, -1, name, -1));
        }

        // 명령어 정렬(시간 순으로 정렬하고, 시간이 같으면 300 출력 명령어가 뒤로 가게 함)
        queries.sort((q1, q2) -> compare(q1, q2) ? -1 : 1);

        // 명령어 처리
        for(Query query : queries) {
            if(query.cmd == 100) {      // 초밥 생성
                cntSushi++; 
            } else if(query.cmd == 111) {       // 초밥을 먹음
                cntSushi--;
            } else if(query.cmd == 200) {       // 사람이 들어옴
                cntPeople++;
            } else if(query.cmd == 222) {       // 사람이 나감
                cntPeople--;
            } else {        // 출력
                bw.write(cntPeople + " " + cntSushi + "\n");   // 남아있는 사람 수와 초밥의 수를 출력
            }
        }

        bw.flush();
    }

    public static boolean compare(Query q1, Query q2) {
        if(q1.t != q2.t) {      // 시각이 다를 때
            return q1.t < q2.t; 
        }
        return q1.cmd < q2.cmd;     // 시각이 같아서 명령어로 비교
    }
}