import java.io.*;
import java.util.StringTokenizer;
import java.util.Deque;
import java.util.ArrayDeque;

public class Main {
    static class Node {
        int left;   // 왼쪽에 있는 선물 번호
        int right;      // 오른쪽에 있는 선물 번호

        Node(int left, int right) {
            this.left = left;
            this.right = right;
        }
    }

    final static int MAX_N = 100000;     // 최대 벨트 갯수
    final static int MAX_M = 100000;     // 최대 선물 갯수

    static int n;       // 밸트 갯수
    static int m;       // 선물의 갯수
    static Deque<Integer>[] deque = new ArrayDeque[MAX_N + 1];        // 벨트
    static Node[] gifts = new Node[MAX_M + 1];      // 선물 배열

    /*
        200: 물건 모두 옮기기
        옮겨진 선물들은 mDst 벨트 앞에 위치한다.
        mSrc 벨트에 선물이 존재하지 않는다면 아무것도 옮기지 않아도 된다.
        옮긴 뒤에 mDst번째 벨트에 있는 선물들의 개수를 출력
    */
    public static int moveAll(int mSrc, int mDst) {
        if(deque[mSrc].isEmpty()) {
            return deque[mDst].size();
        }

        int mSrcLastGift = deque[mSrc].getLast();       // mSrc 벨트에서 가장 마지막 선물의 번호
        int mDstFirstGift = -1;
        if(!deque[mDst].isEmpty()) {
            mDstFirstGift = deque[mDst].getFirst();     // mDst 벨트에서 가장 첫 번째 선물의 번호
        }
        Deque<Integer> tmp = new ArrayDeque();

        tmp.addAll(deque[mSrc]);
        tmp.addAll(deque[mDst]);
        deque[mDst] = tmp;
        deque[mSrc].clear();        // mSrc 벨트에 있는 선물을 모두 없애기

        // mSrc 벨트에서 가장 마지막 선물의 오른쪽 값 = mDst 벨트에서 첫 번째 선물
        gifts[mSrcLastGift].right = mDstFirstGift;   
        // mDst 벨트에서 첫 번째 선물의 왼쪽 값 = mSrc 벨트에서 마지막 선물  
        if(mDstFirstGift != -1) { 
            gifts[mDstFirstGift].left = mSrcLastGift;
        }
        
        return deque[mDst].size();
    }

    /*
        300: 앞 물건만 교체
        옮긴 뒤 m_dst의 선물의 총 수를 출력
        둘 중 하나의 벨트에 선물이 아예 존재하지 않다면 교체하지 않고 해당 벨트로 선물을 옮기기
    */
    public static int change(int mSrc, int mDst) {
        if(deque[mSrc].isEmpty() && deque[mDst].isEmpty()) {      // 두 벨트 모두 선물이 없다면
            return 0;
        } else if(!deque[mSrc].isEmpty() && !deque[mDst].isEmpty()) {   // 두 벨트 모두 선물이 있다면
            int mSrcFront = deque[mSrc].pollFirst();    // mSrc 벨트에서 맨 앞에 있는 선물을 뽑기
            int mDstFront = deque[mDst].pollFirst();    // mDst 벨트에서 맨 앞에 있는 선물을 뽑기
            int tmp = 0;
            
            deque[mSrc].addFirst(mDstFront);        // mSrc 벨트 앞에 mDst에서 뽑은 선물을 추가
            deque[mDst].addFirst(mSrcFront);        // mDst 벨트 앞에 mSrc에서 뽑은 선물을 추가

            // 오른쪽 값을 교환
            tmp = gifts[mSrcFront].right;
            gifts[mSrcFront].right = gifts[mDstFront].right;
            gifts[mDstFront].right = tmp;
        } else if(!deque[mSrc].isEmpty()) {       // mSrc 벨트만 선물이 있다면
            int gift = deque[mSrc].pollFirst();
            deque[mDst].addFirst(gift);     // mDst 벨트로 선물 옮기기
            
            if(gifts[gift].right != -1) {       // 옮길 선물의 뒤에 선물이 있으면
                int right = gifts[gift].right;
                gifts[right].left = -1;     // 선물을 옮겨서 그 뒤에 있던 선물이 가장 맨 앞으로 오게 됨
            }
            gifts[gift].right = -1;
        } else if(!deque[mDst].isEmpty()) {       // mDst 벨트만 선물이 있다면
            int gift = deque[mDst].pollFirst();
            deque[mSrc].addFirst(gift);      // mSrc 벨트로 선물 옮기기

            if(gifts[gift].right != -1) {       // 옮길 선물의 뒤에 선물이 있으면
                int right = gifts[gift].right;
                gifts[right].left = -1;     // 선물을 옮겨서 그 뒤에 있던 선물이 가장 맨 앞으로 오게 됨
            }
            gifts[gift].right = -1;
        }

        return deque[mDst].size();
    }

    /*
        400: 물건 나누기
        mSrc번째 벨트에 있는 선물들의 개수를 n이라고 할 때,
        가장 앞에서 floor(n / 2)번째까지 있는 선물을 mDst번째 벨트 앞으로 옮기기
        진행 이후 mDst의 선물의 총 수를 출력
    */
    public static int divide(int mSrc, int mDst) {
        int n = deque[mSrc].size();     // mSrc번째 벨트에 있는 선물들의 개수
        int moveCnt = (int)Math.floor(n / 2);        // 옮길 선물의 개수
        Deque<Integer> tmp = new ArrayDeque();

        while(moveCnt-- > 0) {
            tmp.add(deque[mSrc].removeFirst());     // mSrc 벨트에서 가장 앞에 있는 선물부터 꺼내서 저장
        }

        if(!tmp.isEmpty()) {
            int mSrcGift = tmp.getLast();   // mSrc 벨트에서 가장 마지막 선물
            int mDstGift = -1;
            if(!deque[mDst].isEmpty()) {
                mDstGift = deque[mDst].getFirst();      // mDst 벨트에서 가장 첫 번째 선물
                tmp.addAll(deque[mDst]);
            }
            deque[mDst] = tmp;

            gifts[mSrcGift].right = mDstGift;
            if(mDstGift != -1) {
                gifts[mDstGift].left = mSrcGift;
            }
        }

        return deque[mDst].size();
    } 

    /*
        (500) 선물 정보 얻기
    */
    public static int getGiftInfo(int pNum) {
        return gifts[pNum].left + 2 * gifts[pNum].right;
    }

    /*
        (600) 벨트 정보 얻기
        bNum: 벨트 번호
    */
    public static int getBeltInfo(int bNum) {
        int a = -1;     // 맨 앞에 있는 선물의 번호
        int b = -1;     // 맨 뒤에 있는 선물의 번호
        int c = 0;      // 선물의 개수

        if(!deque[bNum].isEmpty()) {    // 벨트에 선물이 있다면
            a = deque[bNum].getFirst();
            b = deque[bNum].getLast();
            c = deque[bNum].size();
        }

        return a + 2 * b + 3 * c;
    }

    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(System.out));
        StringTokenizer st;
        int q = Integer.parseInt(br.readLine());        // 명령의 수

        while(q-- > 0) {
            st = new StringTokenizer(br.readLine());
            int cmd = Integer.parseInt(st.nextToken());     // 명령어

            if(cmd == 100) {        // 공장 설립
                n = Integer.parseInt(st.nextToken());     // 벨트 갯수
                m = Integer.parseInt(st.nextToken());     // 선물 갯수

                // 벨트 초기화
                for(int i=1; i<=n; i++) {
                    deque[i] = new ArrayDeque();
                }

                // 선물 초기화
                for(int i=1; i<=m; i++) {
                    gifts[i] = new Node(-1, -1);
                }

                // 벨트에 선물 넣기
                for(int gift=1; gift<=m; gift++) {
                    int bNum = Integer.parseInt(st.nextToken());    // 벨트 번호
                    if(!deque[bNum].isEmpty()) {    // 벨트에 선물이 있으면
                        int lastGift = deque[bNum].getLast();       // 현재 벨트에 있는 가장 마지막 선물
                        gifts[gift].left = lastGift;    // 앞에 있는 선물 번호 저장 
                        gifts[lastGift].right = gift;   // 앞에 있는 선물의 오른쪽 선물 번호를 갱신
                    }
                    deque[bNum].add(gift);      // 벨트에 선물 추가
                }
            } else if(cmd == 200) {     // 물건 모두 옮기기
                int mSrc = Integer.parseInt(st.nextToken());
                int mDst = Integer.parseInt(st.nextToken());

                bw.write(moveAll(mSrc, mDst) + "\n");
            } else if(cmd == 300) {     // 앞 물건만 교체하기
                int mSrc = Integer.parseInt(st.nextToken());
                int mDst = Integer.parseInt(st.nextToken());

                bw.write(change(mSrc, mDst) + "\n");
            } else if(cmd == 400) {     // 물건 나누기
                int mSrc = Integer.parseInt(st.nextToken());
                int mDst = Integer.parseInt(st.nextToken());

                bw.write(divide(mSrc, mDst) + "\n");
            } else if(cmd == 500) {     // 선물 정보 얻기
                int pNum = Integer.parseInt(st.nextToken());

                bw.write(getGiftInfo(pNum) + "\n");
            } else if(cmd == 600) {     // 벨트 정보 얻기
                int pNum = Integer.parseInt(st.nextToken());

                bw.write(getBeltInfo(pNum) + "\n");
            }
        }
        bw.flush();
    }
}