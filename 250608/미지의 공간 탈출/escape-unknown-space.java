import java.util.*;
import java.io.*;

public class Main {
    final static int TIME_WALL_CNT = 5;     // 동, 서, 남, 북 , 윗면
    final static int EAST = 0;      // 동쪽 시간의 벽
    final static int SOUTH = 1;     // 남쪽 시간의 벽
    final static int WEST = 2;      // 서쪽 시간의 벽
    final static int NORTH = 3;     // 북쪽 시간의 벽
    final static int TOP = 4;       // 위쪽 시간의 벽

    static int n;       // 5 <= 미지의 공간 크기 <= 20
    static int m;       // 2 <= 시간의 벽 크기 <= min(n - 2, 10)
    static int f;       // 1 <= 시간 이상 현상의 갯수 <= 10
    static int[][] arr;     // 미지의 공간 평면도
    static int[][] idArr;       // 평면도의 고유 번호
    static int[][][] idTimeWall;    // 시간의 벽의 고유 변호
    static int[][][] timeWall;     // 5개의 m * m 시간의 벽
    static int[][] graph;
    static Position timeMachine;        // 타임머신의 위치
    static Position wayOutPos;          // 탈출구로 이어지는
    static Position timeWallStartPos;
    static List<Anomaly> anomalyLst;        // 이상현상을 저장하는 리스트

    static class Anomaly {
        Position position;
        int d;  // 방향
        int v;      // 배수
        boolean on;     // 확산 가능성

        Anomaly(Position position, int d, int v, boolean on) {
            this.position = position;
            this.d = d;
            this.v = v;
            this.on = on;
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

    /*
        시간의 벽 윗면에서 타임머신의 위치 찾기
    */
    public static void findTimeMachine () {
        for (int i = 0; i < m; i++) {
            for (int j = 0; j < m; j++) {
                if (timeWall[TOP][i][j] == 2) {
                    timeMachine = new Position(i, j);
                    return;
                }
            }
        }
    }

    public static boolean isValidTimeWall ( int x, int y){
        return 0 <= x && x < m && 0 <= y && y < m;
    }

    public static void makeGraph() {
        // 동, 남, 북, 서
        int[] graphDx = {0, 1, 0, -1};
        int[] graphDy = {1, 0, -1, 0};
        int num = 0;        // 고유 번호
        // 평면도에서 시간의 벽을 제외하고 번호를 부여
        for(int i=0; i<n; i++) {
            for (int j = 0; j < n; j++) {
                if(arr[i][j] == 3) {
                    continue;
                }
                idArr[i][j] = ++num;
            }
        }

        // 동쪽 단면도에 번호 부여
        for(int i=0; i<m; i++) {
            for(int j=0; j<m; j++) {
                idTimeWall[EAST][i][j] = ++num;
            }
        }

        // 남쪽 단면도에 번호 부여
        for(int i=0; i<m; i++) {
            for (int j = 0; j < m; j++) {
                idTimeWall[SOUTH][i][j] = ++num;
            }
        }

        // 서쪽 단면도에 번호 부여
        for(int i=0; i<m; i++) {
            for (int j = 0; j < m; j++) {
                idTimeWall[WEST][i][j] = ++num;
            }
        }

        // 북쪽 단면도에 번호 부여
        for(int i=0; i<m; i++) {
            for (int j = 0; j < m; j++) {
                idTimeWall[NORTH][i][j] = ++num;
            }
        }

        // 위쪽 단면도에 번호 부여
        for(int i=0; i<m; i++) {
            for (int j = 0; j < m; j++) {
                idTimeWall[TOP][i][j] = ++num;
            }
        }

        graph = new int[num + 1][4];
        for(int i=0; i<=num; i++) {
            Arrays.fill(graph[i], -1);
        }

        // 평면도에서 시간의 벽(3)을 제외하고 4방 탐색하며 인접한 셀끼리 연결
        for(int i=0; i<n; i++) {
            for (int j = 0; j < n; j++) {
                if(arr[i][j] == 3) {
                    continue;
                }
                for(int d=0; d<4; d++) {
                    int nx = graphDx[d] + i;
                    int ny = graphDy[d] + j;
                    // 격자 바깥을 벗어나거나 시간의 벽이면
                    if(!isValid(nx, ny) || arr[nx][ny] == 3) {
                        continue;
                    }
                    graph[idArr[i][j]][d] = idArr[nx][ny];
                }
            }
        }

        // 단면도를 4방 탐색하며 인접한 셀끼리 연결
        for(int k=0; k<TIME_WALL_CNT-1; k++) {
            for (int i = 0; i < m; i++) {
                for (int j = 0; j < m; j++) {
                    for (int d = 0; d < 4; d++) {
                        int nx = graphDx[d] + i;
                        int ny = graphDy[d] + j;
                        if (nx < 0 || nx >= m) {   // 행이 범위를 넘어가면
                            continue;
                        }
                        if (ny < 0) {
                            graph[idTimeWall[k][i][j]][d] = idTimeWall[(k + 1) % 4][nx][m - 1];
                        } else if(ny >= m) {
                            graph[idTimeWall[k][i][j]][d] = idTimeWall[(k + 3) % 4][nx][0];
                        } else {
                            graph[idTimeWall[k][i][j]][d] = idTimeWall[k][nx][ny];
                        }
                    }
                }
            }
        }

        // 시간의 벽 위쪽 단면도를 4방 탐색하며 인접한 셀끼리 연결
        for(int i=0; i<m; i++) {
            for (int j = 0; j < m; j++) {
                for(int d=0; d<4; d++) {
                    int nx = graphDx[d] + i;
                    int ny = graphDy[d] + j;
                    if (!isValidTimeWall(nx, ny)) {     // 격자를 벗어나면
                        continue;
                    }
                    graph[idTimeWall[TOP][i][j]][d] = idTimeWall[TOP][nx][ny];
                }
            }
        }

        // 시간의 벽 위쪽과 동쪽 단면도의 인접한 셀 연결
        for(int i=0; i<m; i++) {
            int topId = idTimeWall[TOP][i][m - 1];
            int eastId = idTimeWall[EAST][0][m - 1 - i];
            graph[topId][EAST] = eastId;
            graph[eastId][NORTH] = topId;
        }

        // 시간의 벽 위쪽과 남쪽 단면도의 인접한 셀 연결
        for(int i=0; i<m; i++) {
            int topId = idTimeWall[TOP][m - 1][i];
            int southId = idTimeWall[SOUTH][0][i];
            graph[topId][SOUTH] = southId;
            graph[southId][NORTH] = topId;
        }

        // 시간의 벽 위쪽과 서쪽 단면도의 인접한 셀 연결
        for(int i=0; i<m; i++) {
            int topId = idTimeWall[TOP][0][i];
            int westId = idTimeWall[WEST][0][i];
            graph[topId][WEST] = westId;
            graph[westId][NORTH] = topId;
        }

        // 시간의 벽 위쪽과 북쪽 단면도의 인접한 셀 연결
        for(int i=0; i<m; i++) {
            int topId = idTimeWall[TOP][0][i];
            int northId = idTimeWall[NORTH][0][m - 1- i];
            graph[topId][NORTH] = northId;
            graph[northId][NORTH] = topId;
        }

        // 평면도와 동쪽 단면도의 인접한 셀 연결
        if(timeWallStartPos.y + m < n) {
            for(int i=0; i<m; i++) {
                int timeWallId = idTimeWall[EAST][m - 1][i];
                int arrId = idArr[timeWallStartPos.x + (m - 1) - i][timeWallStartPos.y + m];
                graph[timeWallId][SOUTH] = arrId;
                graph[arrId][WEST] = timeWallId;
            }
        }

        // 평면도와 남쪽 단면도의 인접한 셀 연결
        if(timeWallStartPos.x + m < n) {
            for(int i=0; i<m; i++) {
                int timeWallId = idTimeWall[SOUTH][m - 1][i];
                int arrId = idArr[timeWallStartPos.x + m][timeWallStartPos.y + i];
                graph[timeWallId][SOUTH] = arrId;
                graph[arrId][NORTH] = timeWallId;
            }
        }

        // 평면도와 서쪽 단면도의 인접한 셀 연결
        if(timeWallStartPos.y > 0) {
            for(int i=0; i<m; i++) {
                int timeWallId = idTimeWall[WEST][m - 1][i];
                int arrId = idArr[timeWallStartPos.x + i][timeWallStartPos.y - 1];
                graph[timeWallId][SOUTH] = arrId;
                graph[arrId][EAST] = timeWallId;
            }
        }

        // 평면도와 북쪽 단면도의 인접한 셀 연결
        if(timeWallStartPos.x > 0) {
            for(int i=0; i<m; i++) {
                int timeWallId = idTimeWall[NORTH][m - 1][i];
                int arrId = idArr[timeWallStartPos.x - 1][timeWallStartPos.y + (m - 1) - i];
                graph[timeWallId][SOUTH] = arrId;
                graph[arrId][SOUTH] = timeWallId;
            }
        }
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < n && 0 <= y && y < n;
    }

    public static void init(int[] time) {
        Arrays.fill(time, -1);      // 계산하지 않은 값은 -1로 초기화
        // 평면도에 있는 장애물 초기화
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                if(arr[i][j] == 3) {        // 시간의 벽이라면
                    continue;
                }
                if(arr[i][j] == 1) {        // 장애물
                    time[idArr[i][j]] = Integer.MAX_VALUE;
                }
            }
        }

        // 평면도에서 시간 이상 현상 초기화
        for(int i=0; i<f; i++) {
            Anomaly anomaly = anomalyLst.get(i);
            time[idArr[anomaly.position.x][anomaly.position.y]] = Integer.MAX_VALUE;
        }

        // 동, 서, 남, 북, 윗면 단면도의 장애물 초기화
        for(int k=0; k<TIME_WALL_CNT; k++) {
            for(int i=0; i<m; i++) {
                for(int j=0; j<m; j++) {
                    if(timeWall[k][i][j] == 1) {
                        time[idTimeWall[k][i][j]] = Integer.MAX_VALUE;
                    }
                }
            }
        }
    }

    public static int bfs () {
        Queue<Integer> que = new LinkedList();
        int[] time = new int[(n * n) + (4 * m * m) + 1];
        init(time);
        int timeMachineId = idTimeWall[TOP][timeMachine.x][timeMachine.y];
        que.offer(timeMachineId);
        time[timeMachineId] = 0;

        for(int turn=1; ; turn++) {
            for(int i=0; i<f; i++) {        // 이상 현상 갯수만큼 반복
                Anomaly anomaly = anomalyLst.get(i);

                if(!anomaly.on) {       // 더이상 확산할 수 없다면
                    continue;
                }

                if(turn % anomaly.v != 0) {     // 확산할 시점이 아니면
                    continue;
                }

                int nx = anomaly.position.x;
                int ny = anomaly.position.y;
                if(anomaly.d == 0) {
                    ny += (turn / anomaly.v);
                } else if(anomaly.d == 1) {
                    nx += (turn / anomaly.v);
                } else if(anomaly.d == 2) {
                    ny -= (turn / anomaly.v);
                } else {
                    nx -= (turn / anomaly.v);
                }

                // 격자 바깥을 나가거나 장애물/탈출구/시간의 벽을 만나면
                if(!isValid(nx, ny)) {
                    anomalyLst.get(i).on = false;
                    continue;
                }

                time[idArr[nx][ny]] = Integer.MAX_VALUE;      // 타임머신이 지나갈 수 없는 위치
            }

            List<Integer> next = new ArrayList();   // 현재 턴에서 도달 가능한 셀들의 번호 저장
            while(!que.isEmpty()) {
                int id = que.poll();
                for(int d=0; d<4; d++) {
                    int newId = graph[id][d];
                    if(newId == -1) {
                        continue;
                    }
                    if(time[newId] != -1) {     // 이미 계산한 셀이라면
                        continue;
                    }
                    time[newId] = turn;
                    next.add(newId);
                }
            }

            if(next.isEmpty()) {        // 새로 도달할 수 있는 셀이 없으면
                break;
            }

            // 새로 도달할 수 있는 셀들을 큐에 추가
            for(int num : next) {
                que.offer(num);
            }

            if(time[idArr[wayOutPos.x][wayOutPos.y]] != -1) {   // 탈출구까지 오는 데 걸리는 시간을 계산했다면
                return time[idArr[wayOutPos.x][wayOutPos.y]];
            }
        }
        return time[idArr[wayOutPos.x][wayOutPos.y]];
    }

    /*
        평면도에서 시간의 벽이 처음 등장하는 위치 찾기
     */
    public static void findTimeWallStart() {
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                if(arr[i][j] == 3) {
                    timeWallStartPos = new Position(i, j);
                    return;
                }
            }
        }
    }

    /*
        평면도에서 탈출구 위치 찾기
     */
    public static void findWayOut() {
        for(int i=0; i<n; i++) {
            for(int j=0; j<n; j++) {
                if(arr[i][j] == 4) {
                    wayOutPos = new Position(i, j);
                    return;
                }
            }
        }
    }

    public static void main (String[]args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        f = Integer.parseInt(st.nextToken());

        // 미지의 공간 평면도 입력 받기
        arr = new int[n][n];
        idArr = new int[n][n];
        for (int i = 0; i < n; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < n; j++) {
                arr[i][j] = Integer.parseInt(st.nextToken());
            }
        }

        timeWall = new int[TIME_WALL_CNT][m][m];
        idTimeWall = new int[TIME_WALL_CNT][m][m];
        // 시간의 벽 동쪽 단면도 입력받기
        for (int i = 0; i < m; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < m; j++) {
                timeWall[EAST][i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // 시간의 벽 서쪽 단면도 입력받기
        for (int i = 0; i < m; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < m; j++) {
                timeWall[WEST][i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // 시간의 벽 남쪽 단면도 입력받기
        for (int i = 0; i < m; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < m; j++) {
                timeWall[SOUTH][i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // 시간의 벽 북쪽 단면도 입력받기
        for (int i = 0; i < m; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < m; j++) {
                timeWall[NORTH][i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // 시간의 벽 위쪽 단면도 입력받기
        for (int i = 0; i < m; i++) {
            st = new StringTokenizer(br.readLine());
            for (int j = 0; j < m; j++) {
                timeWall[TOP][i][j] = Integer.parseInt(st.nextToken());
            }
        }

        // 시간 이상 현상 입력받기
        anomalyLst = new ArrayList();
        for (int i = 0; i < f; i++) {
            st = new StringTokenizer(br.readLine());
            int x = Integer.parseInt(st.nextToken());
            int y = Integer.parseInt(st.nextToken());
            int d = Integer.parseInt(st.nextToken());
            int v = Integer.parseInt(st.nextToken());
            if(d == 1) d = 2;
            else if(d == 2) d = 1;
            anomalyLst.add(new Anomaly(new Position(x, y), d, v, true));
        }

        // 타임머신의 위치 찾기
        findTimeMachine();

        // 평면도에서 시간의 벽 위치 찾기
        findTimeWallStart();

        // 탈출구 위치 찾기
        findWayOut();

        makeGraph();

        // 타임머신에서 탈출구까지 최단 경로로 이동
        int answer = bfs();

        if(answer == -1 || answer >= Integer.MAX_VALUE) {
            answer = -1;
        }
        System.out.print(answer);
    }
}
