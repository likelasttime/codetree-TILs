import java.io.*;
import java.util.*;

public class Main {
	// 상 우 하 좌
	final static int[] DX = {-1, 0, 1, 0};
	final static int[] DY = {0, 1, 0, -1};
	
	static int n;		// 2 <= 격자 크기 <= 20
	static int m;		// 1 <= 플레이어의 수 <= min(n^2, 30)
	static int k;		// 1 <= 라운드 수 <= 500
	static int[] point;		// 플레이어가 획득한 포인트 배열
	static int[] direction;		// 플레이어의 방향 배열
	static int[] skill;		// 플레이어의 능력치 배열
	static Position[] player;	// 플레이어의 위치 배열
	static PriorityQueue<Gun>[][] pq;	// 총을 저장하는 격자
	static int[][] arr;		// 사람을 저장하는 격자
	static int[] gunAttack;		// 총의 공격력을 저장하는 배열
	static int[] playerGun;		// 플레이어가 보유한 총 번호 배열
	
	static class Position {
		int r;		// 행
		int c;		// 열
		
		Position(int r, int c) {
			this.r = r;
			this.c = c;
		}
		
		public boolean isSamePos(int r, int c) {
			return this.r == r && this.c == c;
		}
	}
	
	static class Gun implements Comparable<Gun> {
		int id;			// 고유 번호
		int attack;		// 공격력 수치
		
		Gun(int id, int attack) {
			this.id = id;
			this.attack = attack;
		}
		
		@Override
		public int compareTo(Gun gun) {
			return gun.attack - this.attack;		// 공격력이 높은 순
		}
		
		@Override
		public boolean equals(Object o) {
			if(this == o) {		// 같은 객체
				return true;
			}
			if(!(o instanceof Gun)) {
				return false;
			}
			Gun gun = (Gun) o;
			return this.id == gun.id; 
		}
		
		@Override
		public int hashCode() {
			return Objects.hash(id);
		}
	}
	
	public static boolean isValid(int x, int y) {
		return 1 <= x && x <= n && 1 <= y && y <= n;
	}
	
	/*
	 * 	num: 플레이어 번호
	 * (r, c): 플레이어가 움직인 위치
	 * (prevR, prevC): 플레이어의 원위치
	 */
	public static void getGun(int num, int r, int c, int prevR, int prevC) {
		Gun maxGun = pq[r][c].peek();		// 현재 위치에서 가장 공격력이 큰 총
		if(playerGun[num] == 0) {		// 플레이어가 총을 미보유
			playerGun[num] = maxGun.id;
		} else {		// 플레이어가 총을 보유
			moveGun(prevR, prevC, r, c, num);
			if(maxGun.attack > gunAttack[playerGun[num]]) {		// 이동한 위치에 있는 총이 공격력이 더 크면
				// 총 획득
				playerGun[num] = maxGun.id;
			}
		}
	}
	
	public static void moveGun(int prevR, int prevC, int r, int c, int num) {
		// 플레이어가 가지고 있던 총을 원위치에서 제거 
		pq[prevR][prevC].remove(new Gun(playerGun[num], gunAttack[playerGun[num]]));
		// 가지고 있던 총을 해당 칸에 두기
		pq[r][c].add(new Gun(playerGun[num], gunAttack[playerGun[num]]));
	}
	
	/*
	 * 진 플레이어는 총을 격자에 버리기
	 * (r, c): 싸움이 벌어진 위치
	 */
	public static void performLoser(int r, int c, int num) {
		int gunId = playerGun[num];		// 가지고 있는 총 번호
		Position cur = player[num];
		playerGun[num] = 0;		// 총 버리기
		Gun gun = new Gun(gunId, gunAttack[gunId]);
		
		if(!cur.isSamePos(r, c) && pq[cur.r][cur.c].contains(gun)) {
			pq[cur.r][cur.c].remove(gun);		// 움직이기 전에 있던 총을 제거
			pq[r][c].add(gun);		// 새 위치로 총을 옮기기
		}
		
		changeLoserPos(num);
	}
	
	/*
	 * num: 플레이어 번호
	 */
	public static void changeLoserPos(int num) {
		int d = direction[num];
		// 원위치
		int x = player[num].r;
		int y = player[num].c;
		// 움직인 위치
		int nx = DX[d] + x;
		int ny = DY[d] + y;
		
		if(!canGo(nx, ny)) {		// 격자밖이거나 다른 플레이어가 있으면
			// 오른쪽으로 90도씩 회전하며 빈칸 찾기
			for(int dir=1; dir<4; dir++) {
				int nd = (d + dir) % 4;		// 오른쪽으로 90도 회전한 방향
				nx = DX[nd] + x;
				ny = DY[nd] + y;
				if(!canGo(nx, ny)) {
					continue;
				} else {
					if(!pq[nx][ny].isEmpty()) {		// 총이 있는 위치라면
						// 공격력이 높은 총을 획득
						getGun(num, nx, ny, x, y);
					}
					// 위치 갱신
					player[num].r = nx;		
					player[num].c = ny;
					arr[nx][ny] = num;
					// 방향 갱신
					direction[num] = nd;
					break;
				}
			}
		} else {
			if(!pq[nx][ny].isEmpty()) {		// 총이 있는 위치라면
				// 공격력이 높은 총을 획득
				getGun(num, nx, ny, x, y);
			}
			// 위치 갱신
			player[num].r = nx;		
			player[num].c = ny;
			arr[nx][ny] = num;
		}
	}
	
	/*
	 * 격자 내에 있고, 다른 플레이어가 없으면
	 */
	public static boolean canGo(int x, int y) {
		return isValid(x, y) && arr[x][y] == 0;
	}
	
	public static void fight(int r, int c, int prevR, int prevC) {
		int prevPlayer = arr[r][c];		// 안 움직인 플레이어
		int nPlayer = arr[prevR][prevC];		// 움직인 플레이어
		// 이동한 위치에 있던 플레이어의 초기 능력치 + 총의 공격력의 합
		int prevVal = skill[prevPlayer] + gunAttack[playerGun[prevPlayer]];
		// 새로온 플레이어의 초기 능력치 + 총의 공격력의 합
		int newVal = skill[nPlayer] + gunAttack[playerGun[nPlayer]];
		int loser = -1;
		int winner = -1;
		
		if(prevVal > newVal) {
			point[prevPlayer] += (prevVal - newVal);
			winner = prevPlayer;
			loser = nPlayer;
		} else if(prevVal < newVal) {
			point[nPlayer] += (newVal - prevVal);
			winner = nPlayer;
			loser = prevPlayer;
		} else {		// 같으면
			// 초기 능력치가 높은 플레이어가 승리
			if(skill[prevPlayer] > skill[nPlayer]) {
				point[prevPlayer] += (prevVal - newVal);
				winner = prevPlayer;
				loser = nPlayer;
			} else {
				point[nPlayer] += (newVal - prevVal);
				winner = nPlayer;
				loser = prevPlayer;
			}
		}
		
		changeArrPos(r, c, prevR, prevC, winner);
		performLoser(r, c, loser);
		if(!pq[r][c].isEmpty()) {
			getGun(winner, r, c, r, c);		// 승자는 공격력이 더 높은 총을 획득
		}
	}
	
	public static void changeArrPos(int r, int c, int prevR, int prevC, int num) {
		arr[prevR][prevC] = 0; 
		arr[r][c] = num;
	}
	
	public static void move() {
		for(int i=1; i<=m; i++) {		// 플레이어 번호
			int d = direction[i];		// 방향
			// 원위치
			int x = player[i].r;
			int y = player[i].c;
			// 새위치
			int nx = DX[d] + x;
			int ny = DY[d] + y;
			if(!isValid(nx, ny)) {		// 격자 바깥을 벗어나면
				d = (d + 2) % 4;	// 반대 방향으로 바꾸기
				// 바꾼 방향대로 1칸 움직인 위치
				nx = DX[d] + x;
				ny = DY[d] + y;
			}
			
			// 방향 갱신
			direction[i] = d;
			// 위치 갱신
			player[i].r = nx;
			player[i].c = ny;
			
			if(arr[nx][ny] != 0) {		// 다른 플레이어가 있으면
				// 총의 위치 갱신
				moveGun(x, y, nx, ny, i);
				fight(nx, ny, x, y);
			}
			// 이동한 칸에 총이 있다면
			else if(!pq[nx][ny].isEmpty()) {
				getGun(i, nx, ny, x, y);
				changeArrPos(nx, ny, x, y, i);
			} else {		// 이동한 칸에 총이 없을 때
				// 총의 위치 갱신
				moveGun(x, y, nx, ny, i);
				// 플레이어의 위치 갱신
				changeArrPos(nx, ny, x, y, i);
			}
		}
	}
	
    public static void main(String[] args) throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine());
        n = Integer.parseInt(st.nextToken());
        m = Integer.parseInt(st.nextToken());
        k = Integer.parseInt(st.nextToken());
        point = new int[m + 1];
        direction = new int[m + 1];
        skill = new int[m + 1];
        player = new Position[m + 1];
        pq = new PriorityQueue[n + 1][n + 1];
        gunAttack = new int[(n + 1) * (n + 1)];
        playerGun = new int[m + 1];
        arr = new int[n + 1][n + 1];
        
        // 총의 정보 입력받기
        int id = 1;		
        for(int i=1; i<=n; i++) {
        	st = new StringTokenizer(br.readLine());
        	for(int j=1; j<=n; j++) {
        		pq[i][j] = new PriorityQueue();
        		int attackVal = Integer.parseInt(st.nextToken());
        		if(attackVal == 0) {		// 총이 없는 빈칸
        			continue;
        		}
        		pq[i][j].add(new Gun(id, attackVal));
        		gunAttack[id] = attackVal;
        		id++;
        	}
        }
        
        // 플레이어들의 정보 입력받기
        for(int i=1; i<=m; i++) {
        	st = new StringTokenizer(br.readLine());
        	int x = Integer.parseInt(st.nextToken());		// 행	
        	int y = Integer.parseInt(st.nextToken());		// 열
        	int d = Integer.parseInt(st.nextToken()); 		// 방향
        	int s = Integer.parseInt(st.nextToken()); 		// 초기 능력치
        	player[i] = new Position(x, y);
        	direction[i] = d;
        	skill[i] = s;
        	arr[x][y] = i;		
        }
        
        for(int round=1; round<=k; round++) {
        	move();
        }
        
        // 각 플레이어가 획득한 포인트 출력
        for(int i=1; i<=m; i++) {
        	System.out.print(point[i] + " ");
        }
        
    }
}