import java.io.*;
import java.util.*;

/*
 * 구현 완료 : 현재 2시간 8분
 * 1차 디버깅 : 13분
 */
/*
 * 0, 1, 2, 3 : 북 동 남 서
R행 C열 격자 - 1-based
격자의 동, 서, 남 은 막혀 있음
K명의 정령, 각자 골렘 탐
골렘 중앙 제외 4칸 중 한 칸은 골렘의 출구
어떤 방향에서든 골렘에 탑승 가능(출구도 가능), 
정해진 출구로만 내릴 수 있음
초기 골렘 출구는 di 방향
정령의 최종 위치는 중복 가능

1. 골렘의 이동
1-1. 남쪽으로 한 칸 이동 , 초록색 세 구역이 비어야 가능
1-2. 1-1로 이동할 수 없으면 서쪽 방향으로 회전하면서 내려감
1-2-1. 왼쪽 이동, 출구 반시계 회전
1-2-2. 아래 이동
1-3. 1-2로 이동 못하면 동쪽 방향으로 회전하면서 내려감
1-3-1. 오른쪽 이동, 출구 시계 회전
1-3-2. 아래 이동
1---. 더이상 이동 못할때까지 1-1,2,3 반복
1---. 숲 범위 밖에 골렘 몸이 나가는지 확인, 나가면 1-7로 해야함 마지막으로 걸쳐서 
나가게 한 얘는 점수 포함 X
1-4. 골렘이 가장 남쪽에 도달해 더이상 이동 못하면 골렘 내에서 상하좌우 인접 칸으로 이동 가능
1-5. 골렘의 출구가 다른 골렘과 인접하고 있으면 해당 출구를 통해 다른 골렘 이동 가능
1-6. 최종적으로 정령의 이동 가능 칸에서 가장 남쪽 칸으로 이동, 이동 완전히 종료
1-7. 정령이 최종적으로 이동해도 골렘의 일부가 숲을 벗어난 상태면 모든 골렘이 숲을 빠져나가고
새롭게 탐색 시작
 */
/* [설계]
 * 0, 1, 2, 3 : 북 동 남 서
 * 숲 : 그리드
 * R+3 * C (1-based)
 * 최종 점수 행 : 행 - 3
 * K : 정령의 수
 * c : 골렘이 출발하는 열, d : 골렘의 출구 방향 정보
 *
 * golrem = {{-1,0}, {0,1}, {1,0}, {0, -1}, {0,0} 5칸
 * 0-based, 1,2,3,4는 북 동 남 서
 * 
 * pos[4]가 중앙
 * Golrem:pos(int[][]), exitDir
 *
 * grid[][] R+3 * C
 * 
 * def moveGolerm:
 * 1. 해당 골렘중심이 (2,c)에서 시작
 * moveSouth
 * 2. 밑으로 한 칸 이동 가능한가 골렘 객체 좌표에 2(남)방향으로 한 칸씩 이동한 부분들이 채워져 있는지 확인
 * 2-1. grid 전부 안채워졌으면 이동
 * 2-2. 골렘의 인덱스 1칸 + 2(남) 칸이 채워졌으면 반시계 회전
 * 2-3. 객체 상태 갱신
 * moveReTime
 * 2-2-0. 왼쪽 회전 전에 체크해야 하는 칸 모두 기록, 해당 칸들 가능한지 체크
 * 2-2-1. 가능하면 이동 시작
 * 2-2-1. 왼쪽으로 골렘 모든 칸
 * 2-2-2. 아래쪽으로 골렘 모든 칸 이동
 * 2-2-3. 출구 방향 반시계 갱신 (dir - 1 + 4)%4
 * 2-2-4. 객체 상태 갱신
 * moveTime 
 * 2-3. 2-2-0. 이동 불가능시, 오른쪽 회전 전에 체크해야 하는 칸 모두 기록 불가능하면 2로 이동
 * 2-3-1. 가능하면 이동 시작
 * 2-2-2. 오른쪽으로 골렘 모든 칸 이동
 * 2-2-3. 아래로 골렘 모든 칸 이동
 * 2-2-4. 출구 방향 시계 갱신 (dir + 1 + 4)% 4
 * 2-2-5. 객체 상태 갱신
 * 3. 완전 이동 실패시 이동 탈출, 보드판 갱신
 * 
 * isValidGolrem
 * 3-0. 숨 범위 밖으로 골렘 몸이 나가는지 체크 - 나가면 리셋 시작
 * 3-1. grid를 초기화하고 다음 골렘의 이동을 시작한다
 * 
 * moveAngel
 * 4. 더이상 이동 불가시 정령 좌표에서 bfs 시작, grid에 적힌 번호가 1 이상이면, 골렘이므로
 * 4-0. 이동 가능하면 이동, 가장 긴 거리 체크
 * 4-1. 골렘이면 출구 번호를 얻는다, 해당 출구에 해당하는 좌표를 미리 구하고
 * 4-2. 해당 좌표면 다른 골렘으로 이동 가능하도록 설정
 * 4-3. 가장 남쪽 칸으로 이동 반복
 * 4-4. 최종적으로 도착하면 점수 추가(도착r-3)
 */
class Main {
	public static void main(String args[]) throws Exception {
//		System.setIn(new FileInputStream("res/input.txt"));

		Scanner sc = new Scanner(System.in);
//		int T;
//		T = sc.nextInt();
//		for (int test_case = 1; test_case <= T; test_case++) {
			init(sc);
			
			simulate();
			
//			System.out.println(test_case+"번 답:");
			System.out.println(answerScore);
//		}
	}

	
	static int[] DR = {-1, 0, 1, 0};
	static int[] DC = {0, 1, 0, -1};
	
	static class Golrem{
		int[][] pos;
		int exitDir;
		
		Golrem(int cr, int cc, int exitDir){
			this.pos = new int[5][2];
			this.pos[4][0] = cr; 
			this.pos[4][1] = cc;
			this.exitDir=exitDir;

			for(int dir=0; dir<4; dir++) {
				pos[dir][0] = cr + DR[dir];
				pos[dir][1] = cc + DC[dir];
			}
		}
	}
	
	static Golrem[] golrems; // 1-based
	static int[][] grid; // 1-based
	static int K; // 정령 수
	static int R, C; // 행, 열
	static int MAXROW;
	static int CENTER=4;
	static int answerScore;
	
	static void init(Scanner sc) {
		R = sc.nextInt();
		C = sc.nextInt();
		K = sc.nextInt();
		
		MAXROW = R+3;
		golrems = new Golrem[K+1];
		grid = new int[MAXROW+1][C+1];
		answerScore = 0;
		
		for(int idx=1; idx<=K; idx++) {
			int c = sc.nextInt();
			int d = sc.nextInt();
			
			golrems[idx] = new Golrem(2, c, d);
		}
	}
	
	static boolean outOfBorder(int r, int c) {
		return r > MAXROW || c < 1 || c > C;
	}
	
	static void simulate() {
		for(int gidx=1; gidx <= K; gidx++) {
			if(!moveGolrem(gidx)) { // 숲 범위 나갔으면
				grid = new int[MAXROW+1][C+1];
				// System.out.println("숲범위 나감");
				continue;
			}
			// System.out.println("moveAngel 호출 전");
			// 숲 범위 안나갔으면
			moveAngel(gidx);
		}
	}
	
	private static void moveAngel(int gidx) {
		int sr = golrems[gidx].pos[CENTER][0];
		int sc = golrems[gidx].pos[CENTER][1];
		
		boolean[][] vis = new boolean[MAXROW+1][C+1];
		vis[sr][sc] = true;
		
		Deque<int[]> q = new ArrayDeque<>();
		q.offer(new int[] {sr, sc});
		
		int maxRow = 1;
		
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int nowr = now[0];
			int nowc = now[1];
			
			// 최대 행 처리
			if(maxRow < nowr) {
				maxRow = nowr;
			}
			
			for(int dir=0; dir<4; dir++) {
				int newr = nowr + DR[dir];
				int newc = nowc + DC[dir];
				
				if(outOfBorder(newr, newc)) continue;
				if(grid[newr][newc] < 1) continue; // 골렘 체크
				if(vis[newr][newc]) continue; // 방문 체크
				
				// 만약 현재 좌표가 출구라면 다른 골렘으로 이동 가능하다
				// 출구가 아니라면 자기 골렘 내에서만 이동 가능하다
				int golremIdx = grid[nowr][nowc];
				int exitDir = golrems[golremIdx].exitDir;
				int[] exitPos = golrems[golremIdx].pos[exitDir];

				// 다른 골렘으로 이동하는 거면
				if(grid[newr][newc] != golremIdx) {
					// 출구라면
					if(exitPos[0] == nowr && exitPos[1] == nowc) {
						vis[newr][newc] = true;
						q.offer(new int[] {newr, newc});
					}else { // 아니면 불가능
						continue;
					}
				}else {
					vis[newr][newc] = true;
					q.offer(new int[] {newr, newc});
				}
			}
		}
		
		if(maxRow - 3 < 4) {
			// System.out.println("정령 탐색 오류");
		}
		// System.out.println(maxRow);
		answerScore += (maxRow-3);
	}

	static boolean outOfForest(int gidx) {
		int CR = golrems[gidx].pos[CENTER][0];
		int CC = golrems[gidx].pos[CENTER][1];
		
		if(CR <= 4) return true;
		else return false;
	}
	
	static boolean moveGolrem(int gidx) {
//		int turncnt=0;
			while(true) {
	//			turncnt++;
				// System.out.println(turncnt+"번째 턴일때 ");
				// 아래 이동
				if(moveSouth(gidx)) {
					continue; // 계속 이동
				}else {
					// 반시계 회전
					if(rotateReTime(gidx)) {
						continue;
					}else {
						// 시계 회전
						if(rotateTime(gidx)) {
							continue;
						}else { 
							// 마지막 이동 실패
							// 숲 범위 나갔는지 확인 해야돼
							if(outOfForest(gidx)) { // 숲 범위 나감
								return false;
							}else {
								// 보드판 갱신
								renewBoard(gidx);
								return true;
							}	
						}
					}
				}
			}
	}

	private static void renewBoard(int gidx) {
		for(int bidx=0; bidx<5; bidx++) {
			int r = golrems[gidx].pos[bidx][0];
			int c = golrems[gidx].pos[bidx][1];
			
			if(grid[r][c] > 0) {
				System.out.println("보드판 갱신 실패");
				System.out.println("중복 좌표 갱신");
			}
			grid[r][c] = gidx;
		}
	}

	private static boolean moveSouth(int gidx) {
		Golrem golrem = golrems[gidx];
		
		// 밑 : 2
		boolean invalid = false;
		for(int blockidx=0; blockidx<5; blockidx++) {
			int r = golrem.pos[blockidx][0];
			int c = golrem.pos[blockidx][1];
			
			int nr = r + DR[2];
			int nc = c + DC[2];
			
			if(outOfBorder(nr, nc)) {
				invalid = true;
				break;
			}
			if(grid[nr][nc] > 0) {
				invalid = true;
				break;
			}
		}
		if(invalid) return false;
		
		// System.out.println("south 이동 가능");
		// 가능하면 이동
		for(int blockidx=0; blockidx<5; blockidx++) {
			int r = golrem.pos[blockidx][0];
			int c = golrem.pos[blockidx][1];
			
			int nr = r + DR[2];
			int nc = c + DC[2];
			
			golrem.pos[blockidx][0] = nr;
			golrem.pos[blockidx][1] = nc;
		}
		return true;
	}
	
	private static boolean rotateReTime(int gidx) {
		int[][] mustNeedPos = {
			{-1, -1}, {0, -2}, {1, -2}, {1, -1}, {2,-1}
		};
		
		int r = golrems[gidx].pos[CENTER][0];
		int c = golrems[gidx].pos[CENTER][1];
		
		// 이동 가능한지만 일단 확인
		for(int pidx=0; pidx<5; pidx++) {
			int nr = r + mustNeedPos[pidx][0];
			int nc = c + mustNeedPos[pidx][1];
			
			if(outOfBorder(nr,nc)) return false;
			if(grid[nr][nc] > 0) return false;
		}
		
		// System.out.println("반시계 회전 이동 가능");
		
		// 실제로 이동, 3(서)로 이동, 2(남) 이동
		for(int bidx=0; bidx<5; bidx++) {
			golrems[gidx].pos[bidx][0] += DR[3];
			golrems[gidx].pos[bidx][1] += DC[3];
			
			golrems[gidx].pos[bidx][0] += DR[2];
			golrems[gidx].pos[bidx][1] += DC[2];
		}
		
		// 출구 반시계 회전
		int exitDir = golrems[gidx].exitDir;
		exitDir = (exitDir - 1 + 1000 * 4) %4;
		golrems[gidx].exitDir = exitDir;
		
		return true;
	}
	
	private static boolean rotateTime(int gidx) {
		int[][] mustNeedPos = {
			{-1, 1}, {0, 2}, {1, 2}, {1, 1}, {2, 1}
		};
		
		int r = golrems[gidx].pos[CENTER][0];
		int c = golrems[gidx].pos[CENTER][1];
		
		// 이동 가능한지만 일단 확인
		for(int pidx=0; pidx<5; pidx++) {
			int nr = r + mustNeedPos[pidx][0];
			int nc = c + mustNeedPos[pidx][1];
			
			if(outOfBorder(nr,nc)) return false;
			if(grid[nr][nc] > 0) return false;
		}
		
//		System.out.println("회전 이동 가능");
		
		// 실제로 이동, 1(동)로 이동, 2(남) 이동
		for(int bidx=0; bidx<5; bidx++) {
			golrems[gidx].pos[bidx][0] += DR[1];
			golrems[gidx].pos[bidx][1] += DC[1];
			
			golrems[gidx].pos[bidx][0] += DR[2];
			golrems[gidx].pos[bidx][1] += DC[2];
		}
		
		// 출구 반시계 회전
		int exitDir = golrems[gidx].exitDir;
		exitDir = (exitDir + 1 + 1000 * 4) %4;
		golrems[gidx].exitDir = exitDir;
		
		return true;
	}
}