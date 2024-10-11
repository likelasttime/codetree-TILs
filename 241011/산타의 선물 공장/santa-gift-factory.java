import java.io.*;
import java.util.StringTokenizer;
import java.util.Arrays;
import java.util.Map;
import java.util.HashMap;
import java.util.List;
import java.util.ArrayList;

public class Main {
    final static int MAX_M = 10;        // 최대 벨트 수
    final static int MAX_N = 100000;   // 최대 물건의 갯수 

    static boolean[] isNormal = new boolean[MAX_M + 1];     // 정상적인 벨트인지 
    static int[] wArr = new int[MAX_N + 1];     // 물건 무게     
    static List<Integer>[] belt = new ArrayList[MAX_M + 1];     // 벨트
    static Map<Integer, Integer> pIdToIdx = new HashMap();  // 고유 번호 -> 인덱스 
    static int n;   // 물건의 갯수
    static int m;   // 벨트 수
    static int[] thing = new int[MAX_N + 1];     // 상자마다 어떤 벨트에 있는지

    /*
        물건 하차
        wMax: 상자의 최대 무게
    */
    public static long down(int wMax) {
        long total = 0;     // 하차된 상자 무게의 총 합
        
        // 1번부터 m번까지 순서대로 벨트 탐색
        for(int btn=1; btn<=m; btn++) {     
            if(belt[btn].isEmpty()) {
                continue;
            }

            if(wArr[belt[btn].get(0)] <= wMax) {  // 맨 앞에 있는 선물의 무게 <= 원하는 상자의 최대 무게
                int thingIdx = belt[btn].remove(0);     // 물건 인덱스 
                total += wArr[thingIdx];   // 상자 하차
                thing[thingIdx] = -1;       // 벨트가 없음
            } else {    // 해당 선물을 맨 뒤로 보내기
                belt[btn].add(belt[btn].remove(0));
            }
        }

        return total;
    }

    /*
        물건 제거
        상자가 있으면 rId 출력
        없으면 -1 출력
    */
    public static int removeThing(int rId) {
        if(!pIdToIdx.containsKey(rId)) {
            return -1;
        }

        int idx = pIdToIdx.get(rId);        // 고유 번호 rId -> 인덱스
        int btn = thing[idx];
        
        if(btn == -1) {      // 벨트에 없다면
            return -1;
        }

        belt[btn].remove(belt[btn].indexOf(idx));     // 벨트에서 물건 제거
        thing[idx] = -1;
        return rId;
    }

    /*
        물건 확인
        fId: 고유 번호
    */
    public static int confirm(int fId) {
        if(!pIdToIdx.containsKey(fId)) {
            return -1;
        }
        return thing[pIdToIdx.get(fId)];
    }

    /*
        벨트 고장
        원래 고장났으면 -1 출력
    */
    public static int notNormal(int bNum) {
        if(!isNormal[bNum]) {
            return -1;
        }

        // 고장이 나지 않은 벨트 찾기
        int cur = (bNum + 1) % m;       // 옮길 벨트 번호
        while(true) {
            if(isNormal[cur]) {     // 고장 나지 않은 벨트를 찾았다면
                break;
            }

            cur = (cur + 1) % m;
        }

        // 고장난 벨트에 있는 물건들의 벨트를 갱신
        for(int i : belt[bNum]) {
            thing[i] = cur;
        }

        belt[cur].addAll(belt[bNum]);
        belt[bNum].clear();
        isNormal[bNum] = false;     // 벨트 고장 처리
        return bNum;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st;
        int q = Integer.parseInt(br.readLine());        // 명령의 수
        Arrays.fill(isNormal, true);    

        // 명령의 정보 입력 받기
        while(q-- > 0) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());     // 명령의 수

            if(cmd == 100) {    // 공장 설립
                n = Integer.parseInt(st.nextToken());   // 물건의 갯수
                m = Integer.parseInt(st.nextToken());   // 벨트 수

                // 고유번호 입력받기
                for(int i=1; i<=n; i++) {
                    int pId = Integer.parseInt(st.nextToken());
                    pIdToIdx.put(pId, i);   // 고유번호 : 인덱스
                }

                // 무게 입력받기
                for(int i=1; i<=n; i++) {
                    int w = Integer.parseInt(st.nextToken());
                    wArr[i] = w;
                }

                // 벨트 초기화
                for(int i=1; i<=m; i++) {
                    belt[i] = new ArrayList();
                }

                // 벨트에 물건 올리기
                int beltIdx = 1;
                int cnt = n/m;  // 벨트당 올릴 물건 갯수
                for(int i=1; i<=n; i++) {
                    belt[beltIdx].add(i);
                    thing[i] = beltIdx;     // i 인덱스 물건은 beltIdx번 벨트에 있음

                    if(i % cnt == 0) {
                        beltIdx++;
                    }
                }
            } else if(cmd == 200) {     // 물건 하차
                int wMax = Integer.parseInt(st.nextToken());    // 최대 무게  

                bw.write(down(wMax) + "\n");
            } else if(cmd == 300) {     // 물건 제거
                int rId = Integer.parseInt(st.nextToken());

                bw.write(removeThing(rId) + "\n");
            } else if(cmd == 400) {     // 물건 확인
                int fId = Integer.parseInt(st.nextToken());     // 고유번호

                bw.write(confirm(fId) + "\n");
            } else if(cmd == 500) {     // 벨트 고장
                int bNum = Integer.parseInt(st.nextToken());    // 벨트 번호
                
                bw.write(notNormal(bNum) + "\n");
            }
        }
        bw.flush();
    }
}