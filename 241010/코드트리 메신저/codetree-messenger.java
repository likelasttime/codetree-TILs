import java.io.*;
import java.util.StringTokenizer;
import java.util.Set;
import java.util.HashSet;

public class Main {
    final static int MAX_N = 100000;    // 최대 채팅방의 수

    static int N, Q;        // 채팅방의 수, 명령의 수
    static boolean[] isNotification = new boolean[MAX_N + 1];   // 알림 수신 여부
    static int[] parents = new int[MAX_N + 1];  // 부모
    static int[] left = new int[MAX_N + 1];     // 왼쪽 자식
    static int[] right = new int[MAX_N + 1];    // 오른쪽 자식
    static int[] authority = new int[MAX_N + 1];    // 각 채팅방의 권한 세기
    static Set<Integer> set = new HashSet();    // 알림을 받을 수 있는 채팅방 번호

    /*
        알림망 설정을 끄거나 켜기
    */
    public static void settingNotification(int c) {
        isNotification[c] = !isNotification[c];
    }

    /*
        c번 채팅방의 권한을 power로 변경
    */
    public static void changeAuthority(int c, int power) {
        authority[c] = power;
    }

    /*
        채팅방 c1, c2의 부모를 교환
    */
    public static void chageParent(int c1, int c2) {
        int[] c1Baby = getBaby(c1);
        int[] c2Baby = getBaby(c2);
        c1Baby[parents[c1]] = c2;
        c2Baby[parents[c2]] = c1;
        
        int tmp = parents[c1];
        parents[c1] = c2;
        c2 = tmp;
    }

    public static int[] getBaby(int c) {
        if(left[parents[c]] == c) {    // c 부모의 왼쪽 자식이 c라면
            return left;
        } 
        return right;
    }

    /*
        알림을 받을 수 있는 채팅방 수 조회
    */
    public static void dfs(int cur, int depth) {
        if(!isNotification[cur]) {   // 알림이 꺼져있다면
            return;     // 더이상 아래로 내려가지 않아도 됨
        }  

        if(authority[cur] >= depth) {  // 권한 크기
            set.add(cur);       // 알림을 받을 수 있는 채팅 방 추가
        }

        if(left[cur] == 0 && right[cur] == 0) {      // 자식이 없으면 leaf 노드
            return;
        }
        
        dfs(left[cur], depth + 1);
        dfs(right[cur], depth + 1);
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());   // 채팅방의 수
        Q = Integer.parseInt(st.nextToken());   // 명령의 수

        // 알림 수신 여부 초기화
        for(int n=1; n<=N; n++) {
            isNotification[n] = true;
        }

        // 명령 입력 받기
        while(Q-- > 0) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());     // 명령어

            if(cmd == 100) {        // 사내 메신저 준비
                // 1 ~ N까지 각 채팅방의 부모 채팅방 번호를 입력 받기
                for(int n=1; n<=N; n++) {
                    int parent = Integer.parseInt(st.nextToken());
                    parents[n] = parent;    // n의 부모는 parent
                    if(left[parent] == 0) {     // 왼쪽 자식이 없으면
                        left[parent] = n;   // 왼쪽 자식이 n
                    } else {
                        right[parent] = n;  // 오른쪽 자식이 n
                    }
                }

                // 1 ~ N까지 각 채팅방의 초기 권한 세기 입력 받기
                for(int n=1; n<=N; n++) {
                    int scale = Integer.parseInt(st.nextToken());
                    authority[n] = scale;   // n번 채팅방의 권한 세기는 scale 
                }
            } else if(cmd == 200) {     // 알림망 설정
                int c = Integer.parseInt(st.nextToken());   // 채팅방 번호

                settingNotification(c);
            } else if(cmd == 300) {     // 권한 세기 변경
                int c = Integer.parseInt(st.nextToken());       // 채팅방 번호
                int power = Integer.parseInt(st.nextToken());   // 권한 세기

                changeAuthority(c, power);
            } else if(cmd == 400) {     // 부모 채팅방 교환
                int c1 = Integer.parseInt(st.nextToken());
                int c2 = Integer.parseInt(st.nextToken());

                chageParent(c1, c2);
            } else if(cmd == 500) {     // 알림을 받을 수 있는 채팅방 수 조회
                int c = Integer.parseInt(st.nextToken());

                dfs(c, 0);
                bw.write((set.size() - 1) + "\n");
                set.clear();
            }
        }
        bw.flush();
    }
}