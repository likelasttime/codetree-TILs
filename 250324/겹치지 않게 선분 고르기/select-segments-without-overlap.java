import java.util.Scanner;

public class Main {
    static int answer;
    static int n;       // 1 <= 선분의 개수 <= 15
    static boolean[] visit;     // 선택한 선분 표시
    static int[][] segments;

    public static boolean isOverlapped(int cur) {
        int curLeft = segments[cur][0];
        int curRight = segments[cur][1];
        for(int idx=0; idx<n; idx++) {
            if(!visit[idx]) {       // 선택하지 않은 선분이라면
                continue;
            } else if(idx == cur) {     // 자기 자신 건너뛰기
                continue;
            }
            int left = segments[idx][0];
            int right = segments[idx][1];
            // 양끝점이 겹치는 경우
            if(left == curLeft || right == curRight) {
                return true;
            }
            // 시작점이 다른 선분의 시작점, 끝점보다 클때
            if(curLeft < left && curRight < left) {
                continue;
            }
            if(curLeft > left && curLeft > right) {
                continue;
            }
            return true;        // 두 선분이 겹치는 경우는 총 4가지
        }
        return false;
    }

    /*
        선분을 선택하는 조합을 바꿔가며 재귀호출
    */
    public static void dfs(int depth, int cnt) {
        if(depth == n) {        // 모든 선분을 골랐으면
            answer = Math.max(answer, cnt);
            return;
        }
        for(int i=depth; i<n; i++) {       // i = 선분 인덱스
            if(!visit[i]) {     // 아직 선택하지 않은 선분이라면
                boolean result = isOverlapped(i);
                int cntLine = 0;
                if(!result) {       // 선분을 그릴 수 있으면
                    cntLine = 1;
                    visit[i] = true;
                }
                dfs(depth + 1, cnt + cntLine);
                visit[i] = false;
            }
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        n = sc.nextInt();
        segments = new int[n][2];
        answer = 0;
        visit = new boolean[n];
        // 선분의 양끝점 좌표 입력받기
        for (int i = 0; i < n; i++) {
            segments[i][0] = sc.nextInt();      // 1 ~ 1,000
            segments[i][1] = sc.nextInt();      // 1 ~ 1,000
        }

        dfs(0, 0);

        // 겹치지 않게 뽑을 수 있는 최대 선분의 수 출력
        System.out.println(answer);
    }
}