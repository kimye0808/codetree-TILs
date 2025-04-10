import java.io.*;
import java.util.*;

/*
 * 설계 : 38분
 * 구현 완료 : 1시간 31분
 * 1차 디버깅 : 2시간 25분, 보드판 갱신 순서 논리 오류
 */
/*
 * n 격자크기, m 플레이어 수, k 라운드 수
 * 방향 d = 상 우 하 좌 (0부터)
 * 한 칸에 총이 여러 개 가능
 * 총 공격력은 1이상
 * 완전 빈 칸 = 0
 * 총은 공격력 밖에 필요 없음, 인트로 관리
 * 
 * PQ<Integer>[][] gunBoard // 내림차순
 * 총 있으면 총 gunboard에 일단 놓고 뽑는 식으로 하면 될듯
 * Player : r, c, dir, stat, gun
 * Player[] players
 * int[] points
 * 
 * def fight: uid1, uid2
 * 1. totalScore 능력치 + 총 합 으로 비교 더 높은 플레이어 선정
 * 1-1. 같으면 능력치 높은 플레이어가 winner
 * 2. winner는 totalScore 차이만큼 포인트 증가
 * 3. loser는 총을 gunBoard에 추가, gun=0
 * 4. loser 자체 dir로 한 칸 이동
 * 4-1. playerBoard[][]가 1이상이거나 outOfBorder 면 90도씩 방향 체크(dir+1)%4
 * 4-2. 아니면 이동
 * 4-3. gunBoard[][] size>0면 센 총 획득
 * 4-4. winner의 총 드랍하고 다시 획득 갱신
 * 4-5. 최종 좌표 갱신, playerBoard 갱신
 * 
 * def movePlayers:
 * 0-0. playerBoard int[][] : 플레이어 idx 저장한다
 * 0. 순회 전 
 * 1. players 순회하면서 플레이어 방향으로 한 칸 이동
 * 1-1. 격자 밖 처리: 정반대 방향으로 바꿔서 이동 (dir+2+1000*4)%4 dir 갱신
 * 2. 이동 칸 gunBoard[nr][nc] size가 0보다 크면
 * 2-1. 플레이어의 총을 gunBoard에 넣고 gunBoard에서 하나 가져온다
 * 3. 이동 칸 에 플레이어가 있다면 fight
 * 4. 없으면 좌표 갱신, playerBoard 갱신
 */
public class Main {
	static BufferedReader br;
	static BufferedWriter bw;
	static StringTokenizer st;
	
	public static void main(String[] args) throws Exception{
		// System.setIn(new FileInputStream("res/input.txt"));
		
		br = new BufferedReader(new InputStreamReader(System.in));
		bw = new BufferedWriter(new OutputStreamWriter(System.out));
		

		
		// int T = Integer.parseInt(br.readLine().trim());
		
		// for(int test_case = 1; test_case <= T; test_case++)
		// {
			StringBuilder sb = new StringBuilder();
		// 	sb.append("#").append(test_case).append(" ");
			
			init();
			
			simulate();
			
			for(int idx=1; idx<=M; idx++) {
				sb.append(points[idx]).append(" ");
			}
			// sb.append("\n");
			bw.write(sb.toString());
		// }
		bw.flush();
		bw.close();
	}

	/*
	 * n 격자크기, m 플레이어 수, k 라운드 수
	 * 방향 d = 상 우 하 좌 (0부터)
	 * 
	 * PQ<Integer>[][] gunBoard // 내림차순
	 * 총 있으면 총 gunboard에 일단 놓고 뽑는 식으로 하면 될듯
	 * Player : r, c, dir, stat, gun, point
	 * Player[] players
	*/
	static class Player{
		int r, c, dir, stat, gun;
		Player(int r, int c, int dir, int stat){
			this.r=r;this.c=c;this.dir=dir;
			this.stat=stat;
			this.gun=0;
		}
	}
	
	static int N, M, K;
	static int[] DR = {-1, 0, 1, 0};
	static int[] DC = {0, 1, 0, -1};
	
	static PriorityQueue<Integer>[][] gunBoard; // 1-based
	static Player[] players; // 1-basd
	static int[] points; // 1-basd
	static int[][] playerBoard;
	
	static void init() throws Exception{
		st = new StringTokenizer(br.readLine().trim());
		N = Integer.parseInt(st.nextToken());
		M = Integer.parseInt(st.nextToken());
		K = Integer.parseInt(st.nextToken());
		
		gunBoard = new PriorityQueue[N+1][N+1];
		for(int r=1; r<=N;r++) {
			for(int c=1; c<=N; c++) {
				gunBoard[r][c] = new PriorityQueue<Integer>(new Comparator<Integer>() {
					@Override
					public int compare(Integer o1, Integer o2) {
						return Integer.compare(o2, o1); // 내림차순
					}
				});
			}
		}
		players = new Player[M+1]; // 1-based
		
		for(int r=1; r<=N; r++) {
			st = new StringTokenizer(br.readLine().trim());
			for(int c=1; c<=N; c++) {
				int gun = Integer.parseInt(st.nextToken());
				if(gun > 0) {
					gunBoard[r][c].add(gun);
				}
			}
		}
		points = new int[M+1]; // 1-based
		playerBoard = new int[N+1][N+1];
		
		for(int mid=1; mid<=M; mid++) {
			st = new StringTokenizer(br.readLine().trim());
			int r = Integer.parseInt(st.nextToken());
			int c = Integer.parseInt(st.nextToken());
			int d = Integer.parseInt(st.nextToken());
			int s = Integer.parseInt(st.nextToken());
			players[mid] = new Player(r,c,d,s);
		}
	}
	
	static void printBoard() {
		for(int r=1; r<=N; r++) {
			for(int c=1; c<=N; c++) {
				System.out.print(playerBoard[r][c]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	static void simulate() {
		for(int pid=1; pid<=M; pid++) {
			int r=players[pid].r;
			int c=players[pid].c;
			
		//	playerBoard[r][c] = pid;
		}
		// printBoard();
		for(int turn=1; turn<=K; turn++) {
			//System.out.println(turn+"턴");
			movePlayers();
			//printBoard();
		}
	}
	
	static boolean outOfBorder(int r, int c) {
		return r<1||c<1||r>N||c>N;
	}
	/*
	 * def movePlayers:
	 * 0-0. playerBoard int[][] : 플레이어 idx 저장한다
	 * 0. 순회 전 
	 * 1. players 순회하면서 플레이어 방향으로 한 칸 이동
	 * 1-1. 격자 밖 처리: 정반대 방향으로 바꿔서 이동 (dir+2+1000*4)%4 dir 갱신
	 * 2. 이동 칸 gunBoard[nr][nc] size가 0보다 크면
	 * 2-1. 플레이어의 총을 gunBoard에 넣고 gunBoard에서 하나 가져온다
	 * 3. 이동 칸 에 플레이어가 있다면 fight
	 * 4. 없으면 좌표 갱신, playerBoard 갱신
	*/
	static void movePlayers() {
		for(int pidx=1; pidx<=M; pidx++) {
			Player p = players[pidx];
			
			int r = p.r;
			int c = p.c;
			
			int nr = r + DR[p.dir];
			int nc = c + DC[p.dir];
			int ndir = p.dir;
			if(outOfBorder(nr, nc)) {
				ndir = (ndir + 2 + 1000 * 4) % 4;
				nr = r + DR[ndir];
				nc = c + DC[ndir];
				players[pidx].dir = ndir;
			}
			// System.out.println(pidx+"처리중");
			// printBoard();
			// System.out.println(playerBoard[nr][nc]);
			if(playerBoard[nr][nc] > 0) {
				fight(pidx, playerBoard[nr][nc], nr, nc);
			}else {
				//System.out.println(pidx);
				if(gunBoard[nr][nc].size() > 0) {
					if(p.gun != 0) {
						gunBoard[nr][nc].add(p.gun);
					}
					p.gun = gunBoard[nr][nc].poll();
				}	
				
				players[pidx].r = nr;
				players[pidx].c = nc;
				playerBoard[r][c] = 0;
				playerBoard[nr][nc] = pidx;
			}
			// System.out.println(pidx+"처리후");
			// System.out.println(r+"|"+c);
			// printBoard();
		}
	}
	
	static int[] whoseWinner(int uid1, int uid2) {
		int uid1Score = players[uid1].gun + players[uid1].stat;
		int uid2Score = players[uid2].gun + players[uid2].stat;
		
		int winner = 0;
		int loser = 0;
		if(uid1Score > uid2Score) {
			winner = uid1;
			loser = uid2;
		}else if(uid1Score == uid2Score) {
			if(players[uid1].stat > players[uid2].stat) {
				winner = uid1;
				loser = uid2;
			}else {
				winner = uid2;
				loser = uid1;
			}
		}else {
			winner = uid2;
			loser = uid1;
		}
		points[winner] += Math.abs(uid2Score - uid1Score);
		
		return new int[] {winner, loser};
	}
	/*
	 * def fight: uid1, uid2
	 * 1. totalScore 능력치 + 총 합 으로 비교 더 높은 플레이어 선정
	 * 1-1. 같으면 능력치 높은 플레이어가 winner
	 * 2. winner는 totalScore 차이만큼 포인트 증가
	 * 3. loser는 총을 gunBoard에 추가, gun=0
	 * 4. loser 자체 dir로 한 칸 이동
	 * 4-1. playerBoard[][]가 1이상이거나 outOfBorder 면 90도씩 방향 체크(dir+1)%4
	 * 4-2. 아니면 이동
	 * 4-3. gunBoard[][] size>0면 센 총 획득
	 * 4-4. winner의 총 드랍하고 다시 획득 갱신
	 * 4-5. 최종 좌표 갱신, playerBoard 갱신
	 * */
	private static void fight(int uid1, int uid2, int r, int c) {
		int[] winnerLoser = whoseWinner(uid1, uid2);
		int winner = winnerLoser[0];
		int loser = winnerLoser[1];
		
		int targetR = r;
		int targetC = c;
		int lr = players[loser].r;
		int lc = players[loser].c;
		
		if(players[loser].gun != 0) {
			gunBoard[lr][lc].add(players[loser].gun);
			players[loser].gun = 0;
		}
		
		int nr = targetR;
		int nc = targetC;
		int ndir = players[loser].dir;
		for(int rotCnt=0; rotCnt < 4; rotCnt++) {
			ndir = (players[loser].dir+rotCnt)%4;
			
			nr = targetR + DR[ndir];
			nc = targetC + DC[ndir];
			
			if(outOfBorder(nr,nc) || (playerBoard[nr][nc] > 0 && playerBoard[nr][nc] != winner)) continue;
			
			if(gunBoard[nr][nc].size() > 0) {
				if(players[loser].gun != 0) {
					gunBoard[nr][nc].add(players[loser].gun);
					players[loser].gun = 0;
				}
				players[loser].gun = gunBoard[nr][nc].poll();
			}
			break;
		}
		
		int wr = players[winner].r;
		int wc = players[winner].c;
		if(players[winner].gun != 0) {
			gunBoard[targetR][targetC].add(players[winner].gun);
			players[winner].gun = 0;
		}
		if(gunBoard[targetR][targetC].size()>0) {
			players[winner].gun = gunBoard[targetR][targetC].poll();	
		}
	
		// 최종 좌표 갱신, playerBoard 갱신
		players[loser].r=nr;
		players[loser].c=nc;
		players[loser].dir=ndir;
		players[winner].r=targetR;
		players[winner].c=targetC;
		
		// System.out.println("w: "+winner +" l: "+loser);
		// System.out.println("lr lc : "+lr+", "+lc);
		// System.out.println("lr lc : "+nr+", "+nc);

		playerBoard[lr][lc] = 0;
		playerBoard[wr][wc] = 0;
		playerBoard[nr][nc] = loser;
		playerBoard[targetR][targetC] = winner;

	}
}
