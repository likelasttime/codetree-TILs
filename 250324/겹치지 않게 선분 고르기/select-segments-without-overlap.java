import java.util.Scanner;
import java.util.List;
import java.util.ArrayList;

public class Main {
    static int answer;
    static int n;       // 1 <= 선분의 개수 <= 15
    static List<Integer> line;    // 선택한 선분 저장
    static int[][] segments;

    public static boolean isOverlapped(int cur) {
        int curLeft = segments[cur][0];
        int curRight = segments[cur][1];
        for(int idx : line) {
            //int left = segments[idx][0];
            //int right = segments[idx][1];
            // 양끝점이 겹치는 경우
            if(segments[idx][0] == curLeft || segments[idx][1] == curRight) {
                return true;
            }
            // 시작점이 다른 선분의 시작점, 끝점보다 클때
            if(curLeft < segments[idx][0] && curRight < segments[idx][0]) {
                continue;
            }
            if(curLeft > segments[idx][0] && curLeft > segments[idx][1]) {
                continue;
            }
            return true;        // 두 선분이 겹치는 경우는 총 4가지
        }
        return false;
    }

    /*
        선분을 선택하는 조합을 바꿔가며 재귀호출
    */
    public static void dfs(int depth) {
        if(depth == n) {
            answer = Math.max(answer, line.size());
            return;
        }
        for(int i=depth; i<n; i++) {       // i = 선분 인덱스
            boolean result = isOverlapped(i);
            if(result) {        // 선분이 겹치면
                continue;
            }
            line.add(i);
            dfs(depth + 1);
            line.remove(line.size() - 1);
            dfs(depth + 1);
        }
    }

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        n = sc.nextInt();
        segments = new int[n][2];
        answer = 0;
        line = new ArrayList();
        // 선분의 양끝점 좌표 입력받기
        for (int i = 0; i < n; i++) {
            segments[i][0] = sc.nextInt();      // 1 ~ 1,000
            segments[i][1] = sc.nextInt();      // 1 ~ 1,000
        }

        dfs(0);

        // 겹치지 않게 뽑을 수 있는 최대 선분의 수 출력
        System.out.println(answer);
    }
}