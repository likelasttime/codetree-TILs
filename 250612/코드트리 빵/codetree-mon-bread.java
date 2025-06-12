import java.util.*;
import java.io.*;

public class Main {
    // 상 좌 우 하
    final static int[] DX = {-1, 0, 0, 1};
    final static int[] DY = {0, -1, 1, 0};

    static int n;       // 2 <= 격자 크기 <= 15
    static int m;       // 1 <= 사람의 수 <= min(n^2, 30)
    static int[][] arr;     // 격자(0: 빈 공간, 1: 베이스 캠프)
    static Position[] personArr;        // 사람 위치 배열
    static Position[] storeArr;         // 편의점 위치 배열
    static List<Position> baseCampLst;      // 베이스 캠프 위치 리스트
    static boolean[] isVisitStore;      // 편의점 도착 여부 배열
    static boolean[] isVisitCamp;       // 베이스 캠프 도착 여부 배열

    static class Position {
        int r;      // 행
        int c;      // 열

        Position(int r, int c) {
            this.r = r;
            this.c = c;
        }
    }

    /*
        (x, y)가 격자 내에 있으면 true 반환
    */
    public static boolean isValid(int x, int y) {
        return 1 <= x && x <= n && 1 <= y && y <= n;
    }

    /*
        (sr, sc): 출발점 좌표
        (er, ec): 도착점 좌표
    */
    public static int bfs(int sr, int sc, int er, int ec) {
        Queue<int[]> que = new LinkedList();
        boolean[][] visit = new boolean[n + 1][n + 1];
        que.offer(new int[]{sr, sc, 0});
        visit[sr][sc] = true;

        while(!que.isEmpty()) {
            int[] cur = que.poll();
            if(cur[0] == er && cur[1] == ec) {        // 편의점에 도착
                return cur[2];      // 거리 반환
            }
            for(int d=0; d<4; d++) {
                int nx = DX[d] + cur[0];
                int ny = DY[d] + cur[1];
                if(!canGo(nx, ny) || visit[nx][ny]) {
                    continue;
                }
                visit[nx][ny] = true;
                que.offer(new int[]{nx, ny, cur[2] + 1});
            }
        }
        return Integer.MAX_VALUE;   // 도착 불가능
    }

    public static boolean canGo(int r, int c) {
        if(!isValid(r, c) || arr[r][c] == -1) {      // 격자를 넘어가거나 이동 불가능한 칸이면
            return false;
        }
        return true;
    }

    /*
        베이스 캠프로 이동
    */
    public static void goToCamp(int num) {
        Position storePos = storeArr[num];      // 편의점 위치
        int tmpR = storePos.r;
        int tmpC = storePos.c;
        int minDistance = Integer.MAX_VALUE;
        for(int r=1; r<=n; r++) {       // 행
            for(int c=1; c<=n; c++) {       // 열
                if(arr[r][c] != 1) {        // 베이스 캠프가 아니면
                    continue;
                }
                // 편의점과 남은 베이스 캠프의 거리 계산
                int curDistance = bfs(storePos.r, storePos.c, r, c);
                if(curDistance < minDistance) {
                    minDistance = curDistance;
                    tmpR = r;
                    tmpC = c;
                }
            }
        }
        
        // 위치 갱신
        personArr[num] = new Position(tmpR, tmpC);
        // 이동 불가로 변경
        arr[tmpR][tmpC] = -1;
    }

    /*
        편의점 이동
    */
    public static void goToStore(int time) {
        List<Integer> arrivedStore = new ArrayList();       // 도착한 편의점 번호
        for(int num=1; num<=m; num++) {     // 사람 번호
            if(time <= num) {      // 아직 격자밖에 있다면
                break;
            }
            if(isVisitStore[num]) {      // 이미 편의점에 도착했으면
                continue;
            }
            int minDistance = Integer.MAX_VALUE;        // 편의점까지 가는 최단거리
            int tmpX = personArr[num].r;
            int tmpY = personArr[num].c;
            for(int d=0; d<4; d++) {
                int nx = DX[d] + personArr[num].r;
                int ny = DY[d] + personArr[num].c;
                if(!canGo(nx, ny)) {
                    continue;
                }
                // 거리 계산
                int curDistance = bfs(nx, ny, storeArr[num].r, storeArr[num].c);
                if(curDistance < minDistance) {
                    minDistance = curDistance;
                    tmpX = nx;
                    tmpY = ny;
                }
            }
            // 위치 갱신
            personArr[num].r = tmpX;
            personArr[num].c = tmpY;
            if(tmpX == storeArr[num].r && tmpY == storeArr[num].c) {        // 편의점에 도착했다면
                arrivedStore.add(num);
                isVisitStore[num] = true;
            }
        }

        // 도착한 편의점은 방문할 수 없게 설정
        for(int i : arrivedStore) {
            arr[storeArr[i].r][storeArr[i].c] = -1;
        }
    }
    
    public static boolean isEnd() {
    	for(int i=1; i<=m; i++) {		// 사람 번호
    		if(!isVisitStore[i]) {		
    			return false;
    		}
    	}
    	return true;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        personArr = new Position[m + 1];

        // 격자 정보 입력받기
        arr = new int[n + 1][n + 1];
        baseCampLst = new ArrayList();
        for(int i=1; i<=n; i++) {       // 행
            st = new StringTokenizer(br.readLine());
            for(int j=1; j<=n; j++) {       // 열
                arr[i][j] = Integer.parseInt(st.nextToken());
                if(arr[i][j] == 1) {        // 베이스 캠프
                    baseCampLst.add(new Position(i, j));
                }
            }
        }
        isVisitCamp = new boolean[baseCampLst.size()];

        // 편의점 위치 입력받기
        storeArr = new Position[m + 1];
        isVisitStore = new boolean[m + 1];
        for(int i=1; i<=m; i++) {
            st = new StringTokenizer(br.readLine());
            int r = Integer.parseInt(st.nextToken());       // 행
            int c = Integer.parseInt(st.nextToken());       // 열
            storeArr[i] = new Position(r, c);
        }

        int time = 1;
        while(true) {
            goToStore(time);
            if(time <= m) {
                goToCamp(time);
            }
            if(isEnd()) {
            	break;
            }
            time++;
        }

        // 모든 사람이 편의점에 도착하는 시간 출력
        System.out.print(time);
    }
}