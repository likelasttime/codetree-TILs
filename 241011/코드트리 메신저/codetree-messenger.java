import java.io.*;
import java.util.StringTokenizer;

public class Main {
    final static int MAX_N = 100000;    // 최대 채팅방의 수
    final static int MAX_AUTHORITY = 22;    // 최대 권한의 크기

    static int N, Q;        // 채팅방의 수, 명령의 수
    static boolean[] isNotification = new boolean[MAX_N + 1];   // 알림 수신 여부
    static int[] parents = new int[MAX_N + 1];  // 부모
    static int[][] dp = new int[MAX_N + 1][MAX_AUTHORITY];  // 알림을 받을 수 있는 채팅방 수를 저장
    static int[] val = new int[MAX_N + 1];    // 알림을 받을 수 있는 채팅방 수
    static int[] authority = new int[MAX_N + 1];        // 권한

    /*
        알림망 설정을 끄거나 켜기
    */
    public static void settingNotification(int c) {
        if(isNotification[c]) {    
            int cur = parents[c];     // 부모 노드 번호
            int depth = 1;      // 트리의 깊이

            while(cur != 0) {     // 루트 노드에 갈때까지
                for(int a=depth; a<=21; a++) {
                    val[cur] += dp[c][a];   // cur 노드가 받을 수 있는 알림의 수 증가
                    
                    if(depth < a) {
                        dp[cur][a - depth] += dp[c][a];
                    }    
                }

                if(isNotification[cur]) {    
                    break;
                }

                cur = parents[cur];     // 부모 노드로 갱신
                depth++;    // 트리 깊이 증가
            }
            isNotification[c] = false;     
        } else {    // 알림이 켜져있지 않다면
            int depth = 1;      // 트리의 깊이
            int cur = parents[c];       // 부모 노드

            while(cur != 0) {       // 루트 노드에 갈때까지
                for(int a=depth; a<=21; a++) {
                    val[cur] -= dp[c][a];

                    if(depth < a) {
                        dp[cur][a - depth] -= dp[c][a];
                    }
                }

                if(isNotification[cur]) {  
                    break;
                }

                depth++;
                cur = parents[cur];     // 부모노드로 갱신
            }
            isNotification[c] = true;       
        }
    }

    /*
        c번 채팅방의 권한을 power로 변경
    */
    public static void changeAuthority(int c, int power) {
        int beforeAuthority = authority[c];     // 변경 전 권한 크기
        dp[c][beforeAuthority]--;   // c번 채팅방의 이전 권한일때 채팅 방의 수 차감
        authority[c] = Math.min(power, 20);     // c번 채팅방의 권한을 power로 변경
        
        // 기존 권한 제거
        if(!isNotification[c]) {
            int cur = parents[c];       // 부모 번호
            int depth = 1;  // 트리의 깊이
            
            while(cur != 0) {   // 루트에 갈때까지
                if(beforeAuthority >= depth) {  // 이전 권한 크기가 현재 트리의 깊이보다 크거나 같다면
                    val[cur]--;
                }
                
                if(beforeAuthority > depth) {   // 이전 권한 크기가 현재 트리의 깊이보다 크다면
                    dp[cur][beforeAuthority - depth]--;
                }

                if(isNotification[cur]) {   
                    break;
                }

                cur = parents[cur];     // 부모 노드로 갱신
                depth++;
            }
        }

        dp[c][power]++;     // 채팅방의 권한 크기가 power일때 채팅방의 수 증가

        if(!isNotification[c]) {
            int cur = parents[c];     // 부모 번호
            int depth = 1;

            while(cur != 0) {
                if(power >= depth) {
                    val[cur]++;
                }
                
                if(power > depth) {
                    dp[cur][power - depth]++;
                }

                if(isNotification[cur]) {
                    break;
                }

                depth++;
                cur = parents[cur];
            }
        }
    }

    /*
        채팅방 c1, c2의 부모를 교환
    */
    public static void chageParent(int c1, int c2) {
        boolean beforeNotification1 = isNotification[c1];
        boolean beforeNotification2 = isNotification[c2];

        if(!isNotification[c1]) {
            settingNotification(c1);
        }

        if(!isNotification[c2]) {
            settingNotification(c2);
        }

        int tmp = parents[c1];
        parents[c1] = parents[c2];
        parents[c2] = tmp;

        if(!beforeNotification1) {
            settingNotification(c1);
        }

        if(!beforeNotification2) {
            settingNotification(c2);
        }
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st = new StringTokenizer(br.readLine());
        N = Integer.parseInt(st.nextToken());   // 채팅방의 수
        Q = Integer.parseInt(st.nextToken());   // 명령의 수

        // 명령 입력 받기
        while(Q-- > 0) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());     // 명령어

            if(cmd == 100) {        // 사내 메신저 준비
                // 1 ~ N까지 각 채팅방의 부모 채팅방 번호를 입력 받기
                for(int n=1; n<=N; n++) {
                    int parent = Integer.parseInt(st.nextToken());
                    parents[n] = parent;    // n의 부모는 parent
                }

                // 1 ~ N까지 각 채팅방의 초기 권한 세기 입력 받기
                for(int n=1; n<=N; n++) {
                    int scale = Integer.parseInt(st.nextToken());
                    authority[n] = Math.min(20, scale);   // n번 채팅방의 권한 세기는 scale 
                }

                // 부모로 거슬러올라가기
                for(int n=1; n<=N; n++) {
                    int cur = n;    // 현재 노드 번호
                    int curAuthority = authority[n];      // 현재 노드의 권한 크기
                    dp[cur][curAuthority]++;    // 현재 노드의 권한에서 갯수 증가

                    while(curAuthority != 0 && parents[cur] != 0) {  // 권한이 남았고, 부모 노드가 루트 노드가 아닐 동안
                        cur = parents[cur];   // 현재 노드를 부모 노드로 갱신
                        curAuthority--;     // 권한 감소

                        if(curAuthority != 0) {     // 권한이 남아있다면
                            dp[cur][curAuthority]++;
                        }
                        
                        val[cur]++;     // 현재 노드가 받을 수 있는 알림 수 증가   
                    }
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

                bw.write(val[c] + "\n");
            }
        }
        bw.flush();
    }
}