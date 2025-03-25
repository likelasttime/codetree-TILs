import java.util.*;
import java.io.*;

public class Main {
    final static int MAX_SIZE = 5;
    final static int[] DX = {-1, 1, 0, 0};
    final static int[] DY = {0, 0, -1, 1};

    static int k;       // 1 <= 탐사의 반복 횟수 <= 10
    static int m;       // 10 <= 벽면에 적힌 유물 조각의 개수 <= 300
    static int[][] arr;
    static int[] treasure;  // m개의 유물 조각 번호 저장
    static int[][] rotateArr;
    static List<Position> generateLst;      // 유물이 사라진 후 다시 생길 위치들
    static int treasureIdx;
    static Position center;

    static class Position implements Comparable<Position> {
        int row;
        int col;
        Position(int row, int col) {
            this.row = row;
            this.col = col;
        }

        @Override
        public int compareTo(Position position) {
            // 열 번호가 작은 순
            if(position.col != this.col) {
                return this.col - position.col;
            }
            return position.row - this.row;     // 행 번호가 큰 순
        }
    }

    public static boolean isValid(int x, int y) {
        return 0 <= x && x < MAX_SIZE && 0 <= y && y < MAX_SIZE;
    }

    /*
        최대 유물 가치 구하기
    */
    public static int getMaxValue(int[][] matrix, boolean isTest) {
        boolean[][] visit = new boolean[MAX_SIZE][MAX_SIZE];
        int maxValue = 0;
        for(int row=0; row<MAX_SIZE; row++) {
            for(int col=0; col<MAX_SIZE; col++) {
                // 방문한 곳은 건너뛰기
                if(visit[row][col]) {
                    continue;
                }
                int[][] copiedMatrix = copied(matrix);
                int result = bfs(copiedMatrix, row, col, visit, isTest);
                if(result >= 3) {
                    maxValue += result;
                    if(!isTest) {
                        matrix = copiedMatrix;
                    }
                }
            }
        }

        if(!isTest) {
            rotateArr = matrix;
        }
        return maxValue;
    }

    public static int bfs(int[][] matrix, int row, int col, boolean[][] visit, boolean isTest) {
        Queue<Position> que = new ArrayDeque();
        visit[row][col] = true;
        que.offer(new Position(row, col));
        int result = 1;
        List<Position> lst = new ArrayList();
        lst.add(new Position(row, col));

        while(!que.isEmpty()) {
            Position cur = que.poll();
            for(int d=0; d<4; d++) {
                int nx = DX[d] + cur.row;
                int ny = DY[d] + cur.col;
                // 유효하지 않은 좌표이거나 방문했거나 같은 종류의 유물 조각이 아니면
                if(!isValid(nx, ny) || visit[nx][ny]
                        || matrix[nx][ny] != matrix[cur.row][cur.col]) {
                    continue;
                }
                visit[nx][ny] = true;
                que.offer(new Position(nx, ny));
                result++;
                if(!isTest) {
                    lst.add(new Position(nx, ny));
                }
            }
        }

        if(!isTest && lst.size() >= 3) {
            for(int i=0; i<lst.size(); i++) {
                Position pos = lst.get(i);
                matrix[pos.row][pos.col] = 0;
            }
        }
        return result;
    }

    /*
        배열 복사
    */
    public static int[][] copied(int[][] origin) {
        int[][] copiedArr = new int[MAX_SIZE][MAX_SIZE];
        for(int row=0; row<MAX_SIZE; row++) {
            copiedArr[row] = origin[row].clone();
        }
        return copiedArr;
    }

    /*
        회전
    */
    public static int[][] rotate(int[][] origin, int centerRow, int centerCol) {
        int[][] copiedArr = copied(origin);
        int startRow = centerRow - 1;
        int startCol = centerCol - 1;
        int endRow = centerRow + 1;
        int endCol = centerCol + 1;
        int colIdx = startCol;
        for(int row=startRow; row<=endRow; row++) {
            int cnt = 0;
            for(int col=startCol; col<=endCol; col++) {
                copiedArr[row][col] = origin[endRow - cnt++][colIdx];
            }
            colIdx++;
        }
        return copiedArr;
    }

    public static void findDisappear() {
        generateLst = new ArrayList();
        for(int row=0; row<MAX_SIZE; row++) {
            for(int col=0; col<MAX_SIZE; col++) {
                if(rotateArr[row][col] == 0) {
                    generateLst.add(new Position(row, col));
                }
            }
        }
        Collections.sort(generateLst);
    }

    /*
        우선순위에 따라 새로운 조각 채우기
     */
    public static void generate() {
        int lstSize = generateLst.size();
        for(int i=0; i<lstSize; i++) {
            Position cur = generateLst.get(i);
            rotateArr[cur.row][cur.col] = treasure[treasureIdx++];
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        k = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        arr = new int[MAX_SIZE][MAX_SIZE];
        treasure = new int[m];
        for(int row=0; row<MAX_SIZE; row++) {
            st = new StringTokenizer(br.readLine());
            for(int col=0; col<MAX_SIZE; col++) {
                arr[row][col] = Integer.parseInt(st.nextToken());
            }
        }
        st = new StringTokenizer(br.readLine());
        for(int i=0; i<m; i++) {
            treasure[i] = Integer.parseInt(st.nextToken());
        }

        rotateArr = arr;
        for(int tc=0; tc<k; tc++) {
            int maxValue = 0;       // 최대 가치
            // 3 * 3 격자 선택
            int prevD = 4;
                for (int row = 1; row <= 3; row++) {
                    for (int col = 1; col <= 3; col++) {
                        int[][] tmp = copied(arr);
                        for(int i=0; i<3; i++) {        // 90도, 180도, 270도 회전
                            tmp = rotate(tmp, row, col);
                            int result = getMaxValue(tmp, true);
                            if(result == maxValue && prevD > i) {       // 최대 가치가 같으면 회전 각도가 낮은 방법 선택
                                maxValue = result;
                                rotateArr = tmp;
                                center = new Position(row, col);
                                prevD = i;
                            } else if(result > maxValue) {        // 최대 가치가 더 큰쪽 선택
                                prevD = i;
                                maxValue = result;
                                rotateArr = tmp;
                                center = new Position(row, col);
                            }
                        }
                    }
                }

            if(maxValue == 0) {
                break;
            }

            int turnAnswer = 0;
            while(true) {
                int resultValue = getMaxValue(rotateArr, false);
                if(resultValue == 0) {
                    break;
                }
                // 유물이 사라진 곳 탐색
                findDisappear();

                // 유물이 사라진 곳에 유물 생성
                generate();
                turnAnswer += resultValue;
            }

            System.out.print(turnAnswer + " ");
            arr = rotateArr;
        }
    }
}