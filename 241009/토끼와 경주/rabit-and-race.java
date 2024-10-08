import java.io.*;
import java.util.StringTokenizer;
import java.util.PriorityQueue;
import java.util.Set;
import java.util.HashSet;
import java.util.Map;
import java.util.HashMap;

public class Main {
    static class Rabbit implements Comparable<Rabbit> {
        Position position;
        int pId;        // 토끼의 고유번호
        int jump;       // 총 점프 횟수

        Rabbit(Position position, int pId, int jump) {
            this.position = position;
            this.pId = pId;
            this.jump = jump;
        }

        @Override
        public int compareTo(Rabbit other) {
            if(this.jump != other.jump) {
                return Integer.compare(this.jump, other.jump);  // 총 점프 횟수가 적은 순
            }

            if(this.position.x + this.position.y != other.position.x + other.position.y) {
                // 현재 서있는 행 번호 + 열 번호가 작은 순
                return Integer.compare(this.position.x + this.position.y, other.position.x + other.position.y);     
            }

            if(this.position.x != other.position.x) {
               return Integer.compare(this.position.x, other.position.x);        // 행 번호가 작은 순 
            }

            if(this.position.y != other.position.y) {
               return Integer.compare(this.position.y, other.position.y);       // 열 번호가 작은 순 
            }

            return Integer.compare(this.pId, other.pId);        // 고유번호가 작은 순
        }
    }

    static class Position {
        int x;
        int y;

        Position(int x, int y) {
            this.x = x;
            this.y = y;
        }
    }

    final static int MAX_P = 2000;      // 최대 토끼 수 

    static int N;
    static int M;
    static int P;
    static int totalScore;      // 공통으로 받은 점수
    static int[] scores = new int[MAX_P + 1];   // 각 토끼마다 획득한 점수
    static int[] distances = new int[MAX_P + 1];    // 각 토끼마다 이동할 수 있는 거리
    static int[] jumpArr = new int[MAX_P + 1];   // 각 토끼마다 점프 횟수
    static int[] pIdArr = new int[MAX_P + 1];       // 인덱스: 입력으로 주어진 순서, 값: 토끼의 고유 번호
    static Position[] positions = new Position[MAX_P + 1];       // 토끼마다 위치 저장
    static Map<Integer, Integer> pIdToIdxMap = new HashMap();    // 토끼의 고유 번호 : 인덱스 값

    /*
        토끼가 위로 이동
        curRabbit: 움직일 토끼 객체
        distance: 이 토끼가 움직일 수 있는 거리
    */
    public static Rabbit getUpRabbit(Rabbit curRabbit, int distance) {
        Rabbit upRabbit = curRabbit;    
        distance %= 2 * (N - 1);        

        if(distance >= upRabbit.position.x - 1) {
            distance -= (upRabbit.position.x - 1);      // 남은 이동 거리
            upRabbit.position.x = 1;     // 첫 행으로 설정
        } else {
            upRabbit.position.x -= distance;
            distance = 0;       
        }

        if(distance >= N - upRabbit.position.x) {
            distance -= (N - upRabbit.position.x);
            upRabbit.position.x = N;
        } else {
            upRabbit.position.x += distance;
            distance = 0;
        }
        upRabbit.position.x -= distance;        // distance만큼 위로 이동
        return upRabbit;
    }

    /*
        토끼가 아래로 이동
        curRabbit: 움직일 토끼 객체
        distance: 이 토끼가 움직일 수 있는 거리
    */
    public static Rabbit getDownRabbit(Rabbit curRabbit, int distance) {
        Rabbit downRabbit = curRabbit;      
        distance %= 2 * (N - 1);

        if(distance >= N - downRabbit.position.x) {
            distance -= (N - downRabbit.position.x);
            downRabbit.position.x = N;      // 마지막 행으로 설정
        } else {
            downRabbit.position.x += distance;
            distance = 0;
        }

        if(distance >= downRabbit.position.x - 1) {
            distance -= (downRabbit.position.x - 1);
            downRabbit.position.x = 1;      // 첫 행으로 설정
        } else {
            downRabbit.position.x -= distance;
            distance = 0;
        }
        downRabbit.position.x += distance;      // distance만큼 아래로 이동
        return downRabbit;
    }

    /*
        왼쪽으로 이동
        curRabbit: 움직일 토끼 객체
        distance: 이 토끼가 움직일 수 있는 거리
    */
    public static Rabbit getLeftRabbit(Rabbit curRabbit, int distance) {
        Rabbit leftRabbit = curRabbit;
        distance %= 2 * (M - 1);

        if(distance >= leftRabbit.position.y - 1) {
            distance -= (leftRabbit.position.y - 1);
            leftRabbit.position.y = 1;      // 첫 열로 설정
        } else {
            leftRabbit.position.y -= distance;
            distance = 0;
        }

        if(distance >= M - leftRabbit.position.y) {
            distance -= (M - leftRabbit.position.y);
            leftRabbit.position.y = M;      // 마지막 열로 설정
        } else {
            leftRabbit.position.y += distance;
            distance = 0;
        }
        leftRabbit.position.y -= distance;
        return leftRabbit;
    }

    /*
        오른쪽으로 이동
        curRabbit: 움직일 토끼 객체
        distance: 이 토끼가 움직일 수 있는 거리
    */
    public static Rabbit getRightRabbit(Rabbit curRabbit, int distance) {
        Rabbit rightRabbit = curRabbit;
        distance %= 2 * (M - 1);

        if(distance >= M - rightRabbit.position.y) {
            distance -= (M - rightRabbit.position.y);
            rightRabbit.position.y = M;     // 마지막 열로 설정
        } else {
            rightRabbit.position.y += distance;
            distance = 0;
        }

        if(distance >= rightRabbit.position.y - 1) {
            distance -= (rightRabbit.position.y - 1);
            rightRabbit.position.y = 1;     // 첫 열로 설정
        } else {
            rightRabbit.position.y -= distance;
            distance = 0;
        }
        rightRabbit.position.y += distance;
        return rightRabbit;
    }

    public static boolean cmp(Rabbit rabbit1, Rabbit rabbit2) {
        if(rabbit1.position.x + rabbit1.position.y != rabbit2.position.x + rabbit2.position.y) {
            // 행 번호 + 열 번호가 큰 칸 순으로
            return rabbit2.position.x + rabbit2.position.y > rabbit1.position.x + rabbit1.position.y;
        }

        if(rabbit1.position.x != rabbit2.position.x) {
            return rabbit2.position.x > rabbit1.position.x;     // 행 번호가 큰 순으로
        }

        if(rabbit1.position.y != rabbit2.position.y) {
            return rabbit2.position.y > rabbit1.position.y;       // 열 번호가 큰 순으로
        }

        return rabbit2.pId > rabbit1.pId;       // 고유번호가 큰 순으로
    }

    /*
        K: 턴수
        S: 점수
        경주 진행
    */
    public static void race(int K, int S) {
        Set<Integer> pIdSet = new HashSet();        // 턴을 진행할 동안에 뽑혔던 토끼 번호 저장
        PriorityQueue<Rabbit> rabbitPq = new PriorityQueue();       // 토끼들을 저장하는 우선순위 큐

        // 우선순위 큐 추가
        for(int p=0; p<P; p++) {
            rabbitPq.offer(new Rabbit(new Position(positions[p].x, positions[p].y), pIdArr[p], jumpArr[p]));
        }

        for(int k=0; k<K; k++) {    // 턴수
            Rabbit rabbit = rabbitPq.poll();      // 우선순위가 가장 높은 토끼 
            Position curPosition = rabbit.position;     // 우선순위가 가장 높은 토끼의 현재 위치
            Rabbit nextRabbit = new Rabbit(new Position(0, 0), rabbit.pId, rabbit.jump);        // rabbit이 움직인 후 객체
            int distance = distances[pIdToIdxMap.get(rabbit.pId)];  // 고유번호가 pId인 토끼의 이동 가능한 거리
            
            // 위로 이동
            Rabbit upRabbit = getUpRabbit(new Rabbit(new Position(rabbit.position.x, rabbit.position.y), rabbit.pId, rabbit.jump), distance);
            if(cmp(nextRabbit, upRabbit)) {
                nextRabbit = upRabbit;
            }

            // 아래로 이동
            Rabbit downRabbit = getDownRabbit(new Rabbit(new Position(rabbit.position.x, rabbit.position.y), rabbit.pId, rabbit.jump), distance);
            if(cmp(nextRabbit, downRabbit)) {
                nextRabbit = downRabbit;
            }

            // 왼쪽으로 이동
            Rabbit leftRabbit = getLeftRabbit(new Rabbit(new Position(rabbit.position.x, rabbit.position.y), rabbit.pId, rabbit.jump), distance);
            if(cmp(nextRabbit, leftRabbit)) {
                nextRabbit = leftRabbit;
            }

            // 오른쪽으로 이동
            Rabbit rightRabbit = getRightRabbit(new Rabbit(new Position(rabbit.position.x, rabbit.position.y), rabbit.pId, rabbit.jump), distance); 
            if(cmp(nextRabbit, rightRabbit)) {
                nextRabbit = rightRabbit;
            }
            
            nextRabbit.jump++;      // 점프 횟수 증가
            rabbitPq.offer(nextRabbit);     // 우선순위 큐에 추가
            positions[pIdToIdxMap.get(nextRabbit.pId)] = new Position(nextRabbit.position.x, nextRabbit.position.y);    // 위치 갱신
            pIdSet.add(nextRabbit.pId);    // 달린 토끼에 추가

            // 점수 부여 
            scores[pIdToIdxMap.get(rabbit.pId)] -= (nextRabbit.position.x + nextRabbit.position.y);    // 점프한 토끼는 점수를 못 받음
            totalScore += (nextRabbit.position.x + nextRabbit.position.y);
        }

        // 턴이 끝난 후 가장 우선순위가 높은 토끼를 골라 점수 S를 부여
        Rabbit bestRabbit = new Rabbit(new Position(0, 0), 0, 0);
        while(!rabbitPq.isEmpty()) {
            Rabbit curRabbit = rabbitPq.poll();     

            if(!pIdSet.contains(curRabbit.pId)) {       // 점프한 토끼가 아니라면
                continue;
            }

            if(cmp(bestRabbit, curRabbit)) {
                bestRabbit = curRabbit;
            }
        }
        scores[pIdToIdxMap.get(bestRabbit.pId)] += S;
    }

    /*
        pId: 토끼의 고유번호
        L: 배수
        이동거리 변경
    */
    public static void changeDistance(int idx, int L) {
        distances[idx] *= L;
    }

    /*
        최고의 토끼 선정
    */
    public static int pickBestRabbit() {
        int bestScore = 0;
        for(int p=0; p<P; p++) {
            bestScore = Math.max(bestScore, scores[p] + totalScore);
        }
        return bestScore;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st;
        int Q = Integer.parseInt(br.readLine());      // 명령의 수
        // 명령의 정보 입력받기
        for(int q=0; q<Q; q++) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());     // 명령어

            if(cmd == 100) {        // 경주 시작 준비
                N = Integer.parseInt(st.nextToken());       // 행 크기
                M = Integer.parseInt(st.nextToken());       // 열 크기
                P = Integer.parseInt(st.nextToken());       // 토끼의 수

                // 토끼의 수만큼 입력받기
                for(int p=0; p<P; p++) {        
                    int pId = Integer.parseInt(st.nextToken());     // 고유 번호
                    int d = Integer.parseInt(st.nextToken());       // 이동해야 하는 거리
                    
                    distances[p] = d;     // 토끼의 이동거리를 d로 저장
                    pIdArr[p] = pId;        // p번째로 등장한 토끼의 고유 번호는 pId다.
                    pIdToIdxMap.put(pId, p);      // pId : 인덱스
                    positions[p] = new Position(1, 1);      // 초기 위치는 (1, 1)
                }
            } else if(cmd == 200) {     // 경주 진행
                int K = Integer.parseInt(st.nextToken());       // 턴수
                int S = Integer.parseInt(st.nextToken());       // 점수

                race(K, S);
            } else if(cmd == 300) {     // 이동거리 변경
                int pId = Integer.parseInt(st.nextToken());     // 고유번호
                int L = Integer.parseInt(st.nextToken());       // 이동거리 배수
                
                changeDistance(pIdToIdxMap.get(pId), L);
            } else if(cmd == 400) {     // 최고의 토끼 선정
                System.out.println(pickBestRabbit());
            }
        }
    }
}