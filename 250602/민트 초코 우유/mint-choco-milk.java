import java.util.*;
import java.io.*;

public class Main {
    // 상하좌우
    final static int[] DX = {-1, 1, 0, 0};
    final static int[] DY = {0, 0, -1, 1};

    static int n;       // 1 <= 책상 배열 크기 <= 50
    static int[][] b;       // 학생들의 신앙심
    static String[][] foodArr;     // 신봉음식
    static PriorityQueue<Leader> leaderPq;      // 대표자 우선순위 큐
    static boolean[][] defend;          // 방어 여부 배열
    static Map<String, Integer> foodMap = new HashMap();

    static class Position {
        int row;
        int col;

        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }

    static class Leader implements Comparable<Leader> {
        int row;        // 행
        int col;        // 열
        int len;        // 신봉 음식 길이
        int belief;     // 신앙심

        Leader(int row, int col, int len, int belief) {
            this.row = row;
            this.col = col;
            this.len = len;
            this.belief = belief;
        }

        @Override
        public int compareTo(Leader leader) {
            if(this.len != leader.len) {
                return this.len - leader.len;       // 오름차순
            }
            if(this.belief != leader.belief) {
                return leader.belief - this.belief;        // 신앙심이 높은 순
            }
            if(this.row != leader.row) {
                return this.row - leader.row;       // 행이 작은 순
            }
            return this.col - leader.col;       // 열이 작은 순
        }
    }

    static class Person implements Comparable<Person> {
        int row;    // 행
        int col;    // 열
        int bSize;  // 신앙심 크기

        Person(int row, int col, int bSize) {
            this.row = row;
            this.col = col;
            this.bSize = bSize;
        }

        @Override
        public int compareTo(Person person) {
            if(this.bSize != person.bSize) {
                return person.bSize - this.bSize;    // 신앙심이 높은 순
            }
            if(this.row != person.row) {
                return this.row - person.row;       // 행이 작은 순
            }
            return this.col - person.col;       // 열이 작은 순
        }
    }

    /*
        1) 아침 시간에는 모든 학생의 신앙심이 1씩 증가
    */
    public static void morning() {
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                b[i][j]++;
            }
        }
    }

    /*
        2) 점심 시간
    */
    public static void lunch() {
        boolean[][] visit = new boolean[n][n];      // 방문 배열
        leaderPq = new PriorityQueue();     // 대표자를 담은 우선순위 큐 초기화
        for(int i=0; i<n; i++) {        // 시작 행
            for(int j=0; j<n; j++) {        // 시작 열
                if(visit[i][j]) {       // 이미 방문한 곳은 건너뛰기
                    continue;
                }
                PriorityQueue<Person> pq = findGroupBfs(i, j, visit);   // 신봉음식이 같은 인접한 그룹 찾기
                sendBelief(pq);     // 구성원들의 신앙심을 대표자에게 전달
            }
        }
    }

    /*
        3) 저녁 시간
    */
    public static void dinner() {
        defend = new boolean[n][n];     // 방어 배열 초기화
        // 단일 > 이중 > 삼중 조합 그룹 순서대로 진행
        while(!leaderPq.isEmpty()) {
            Leader cur = leaderPq.poll();       // 대표자
            if(defend[cur.row][cur.col]) {      // 방어 상태에 있으면
                continue;
            }
            int dir = b[cur.row][cur.col] % 4;      // 전파 방향
            int x = b[cur.row][cur.col] - 1;        // 간절함
            b[cur.row][cur.col] = 1;
            spread(dir, cur.row, cur.col, x);
        }
        printBelief();
    }

    /*
        (row, col)에서 시작해서 dir방향으로 전파
        hp: 간절함
    */
    public static void spread(int dir, int row, int col, int hp) {
        int cnt = 0;        // 움직인 횟수
        while(true) {
            cnt++;      // 이동 횟수 증가
            int nx = (DX[dir] * cnt) + row;
            int ny = (DY[dir] * cnt) + col;
            // 격자 밖으로 나가거나 간절함이 다떨어지면
            if(!isValid(nx, ny) || hp <= 0) {
                return;
            }
            // 전파 대상의 신봉 음식과 전파자의 신봉 음식이 같으면
            if(foodArr[nx][ny].equals(foodArr[row][col])) {
                continue;
            }
            // 전파하기
            if(hp > b[nx][ny]) {     // 강한 전파
                // 전파자의 신봉음식과 동일한 음식을 신봉
                foodArr[nx][ny] = foodArr[row][col];
                hp -= (b[nx][ny] + 1);
                b[nx][ny]++;        // 전파대상의 신앙심 증가
                defend[nx][ny] = true;
            } else {        // 약한 전파
                // 기존에 관심을 가지던 기본 음식들과 전파자가 관심을 가지고 있는 기본 음식 모두 합친 음식을 신봉
                foodArr[nx][ny] = getNewFood(foodArr[nx][ny], foodArr[row][col]);
                b[nx][ny] += hp;
                hp = 0;
                defend[nx][ny] = true;
                return;
            }
        }
    }

    public static int getFoodIndex(char ch) {
        switch (ch) {
            case 'T':
                return 0;
            case 'M':
                return 1;
        }
        return 2;       // C
    }

    /*
        기존에 관심을 가지던 기본 음식들과 전파자가 관심을 가지고 있는 기본 음식 모두 합친 음식을 반환
    */
    public static String getNewFood(String original, String bonus) {
        boolean[] checkFood = new boolean[3];
        for(int i=0; i<original.length(); i++) {
            checkFood[getFoodIndex(original.charAt(i))] = true;
        }
        for(int i=0; i<bonus.length(); i++) {
            checkFood[getFoodIndex(bonus.charAt(i))] = true;
        }
        if(!checkFood[0] && checkFood[1] && checkFood[2]) {
            return "CM";
        }
        if(checkFood[0] && checkFood[2] && !checkFood[1]) {
            return "TC";
        }
        if(checkFood[0] && checkFood[1] && !checkFood[2]) {
            return "TM";
        }
        return "TCM";
    }

    /*
        대표자를 제외한 그룹원들은 각자 신앙심을 1씩 대표자에게 넘긴다
    */
    public static void sendBelief(PriorityQueue<Person> pq) {
        Person leader = pq.poll();      // 그룹의 대표자
        leaderPq.offer(new Leader(leader.row, leader.col, foodArr[leader.row][leader.col].length(), b[leader.row][leader.col] + pq.size() - 1));
        while(!pq.isEmpty()) {
            Person person = pq.poll();
            b[leader.row][leader.col]++;        // 대표자에게 신앙심 넘기기
            b[person.row][person.col]--;        // 구성원의 신앙심 감소
        }
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    /*
        4방 탐색을 하며 신봉음식이 같은 그룹을 찾고 반환
    */
    public static PriorityQueue<Person> findGroupBfs(int x, int y, boolean[][] visit) {
        Queue<Position> que = new LinkedList();
        PriorityQueue<Person> pq = new PriorityQueue();
        pq.offer(new Person(x, y, b[x][y]));
        que.offer(new Position(x, y));
        visit[x][y] = true;
        String food = foodArr[x][y];       // 찾을 신봉음식
        while(!que.isEmpty()) {
            Position cur = que.poll();
            for(int d=0; d<4; d++) {
                int nx = DX[d] + cur.row;
                int ny = DY[d] + cur.col;
                // 격자 바깥을 벗어나거나 이미 방문했거나 신봉 음식이 다르면
                if(!isValid(nx, ny) || visit[nx][ny] || !food.equals(foodArr[nx][ny])) {
                    continue;
                }
                visit[nx][ny] = true;
                que.offer(new Position(nx, ny));
                pq.offer(new Person(nx, ny, b[nx][ny]));
            }
        }
        return pq;
    }

    /*
        각 음식의 신봉자들의 신앙심 총합 출력
    */
    public static void printBelief() {
        int[] answer = new int[7];
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                answer[foodMap.get(foodArr[i][j])] += b[i][j];
            }
        }
        for(int i=0; i<answer.length; i++) {
            System.out.print(answer[i] + " ");
        }
        System.out.println();
    }

    /*
        foodMap을 초기화
    */
    public static void init() {
        foodMap.put("TCM", 0);      // 민트초코우유
        foodMap.put("TC", 1);       // 민트초코
        foodMap.put("TM", 2);       // 민트우유
        foodMap.put("CM", 3);       // 초코우유
        foodMap.put("M", 4);        // 우유
        foodMap.put("C", 5);        // 초코
        foodMap.put("T", 6);        // 민트
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        int t = Integer.parseInt(st.nextToken());
        init();
        // 학생의 초기 신봉 음식 입력받기
        foodArr = new String[n][n];
        for(int i=0; i<n; i++) {
            String str = br.readLine();
            for(int j=0; j<n; j++) {
                foodArr[i][j] = String.valueOf(str.charAt(j));
            }
        }
        // 학생의 초기 신앙심 입력받기
        b = new int[n][n];
        for(int i=0; i<n; i++) {
            st = new StringTokenizer(br.readLine());
            for(int j=0; j<n; j++) {
                b[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        for(int day=1; day<=t; day++) {
            morning();
            lunch();
            dinner();
        }
    }
}