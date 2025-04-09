import java.io.*;
import java.util.*;
/* 
 * 최단경로 : 맨해튼 거리 <- 인줄 알았는데 예제 1번에서 (0,3)으로 가면 무한 루프되어서
 * 이상해서 토론 보니까
 * 메두사의 이동은 최단경로, 전사의 이동 네 단계가 맨해튼 거리라고 함
 * 이거 글이 너무 이상하네 
전사의 이동
전사:객체 : r, c, idx, isDead, stoned
warExists: List<widx>[r][c] 보드판 - 생명주기 : 턴마다 초기화, 전사 객체 순회하면서 보드판 좌표 채우자
좌표 갱신도 처리해줘야 함
globalDirBoard: int[N][N] 보드판 - 생명주기 : 매 턴에 초기화
globalWarMoveCnt: 전사 전체 이동 횟수
globalStoneCnt: 메두사에 의해 돌이 된 전사 수

etc. getMedusaDir

etc. getWolfDir

0. warExists 초기화
0-1. 전사 객체 순회하면서 보드판 좌표를 채운다

1. 메두사의 이동 - moveMedusa
1-1. 1로는 이동 불가, 공원까지 최단 거리로 향하는 방향 구하고, 해당 칸으로 이동
1-1-1. 방향이 없으면, 못가므로 종료
1-2. 이동시 warExists 해당 좌표에 존재하는 전사 idx를 가져와서  싹 다 사망 처리한다
1-2-1. 해당 좌표의 warExists를 비운다

2. 메두사의 시선 - watchMedusa
2-0-0. candDirBoard[4][N][N]: 네 방향 dirBoard를 저장하는 보드판
2-0-1. stoneCnt[4] : 네 방향의 돌 수를 저장하는 배열
2-0-2. List<wIdx>[4] stonedWarriors : 네 방향에서 돌 된 워리어들 idx 리스트
2-0. 상,하,좌,우 네 방향으로 본다
2-1. dirBoard를 만든다 초기 -1(HIDDEN)로 채운다
2-2. 메두사의 시선 방향에 따라 다음 세칸을 특정 방향으로 채운다
2-3. 세 칸에 대해서 각 칸들에 해당하는 특정 방향들로만 bfs한다, 그 세 칸도 채운다
2-3-1. 메두사가 하단 방향 보면, (1,-1):좌하단 대각 + 하단, (1,0): 하단, (1,1):우하단 대각 + 하단
2-4. dirBoard에 특정방향들을 저장한다
2-5. dirBoard를 순회하면서 dirBoard가 0이상이면(메두사 시선방향)
2-6. warExists.size가 0보다 큰지 확인, 전사 존재하면 dirBoard의 방향 + 직진 방향으로 bfs
2-6-1. bfs하면서 dirBoard를 -1(HIDDEN)로 채운다
2-7. 다시 dirBoard를 순회하면서 dirBoard가 0이상이서 warExists가 0보다 큰 수를 카운트한다 
2-8. 가장 많은 카운팅 된 방향으로 globalDirBoard를 candDirBoard[방향] 으로 초기화한다
2-8-1. 해당 방향의 stonedWarriors를 돌면서 stoned 상태체크를 해준다
2-9. globalStoneCnt를 stoneCnt[방향] 누적한다

3. 전사들의 이동 - moveWarrior
최대 두 칸 이동가능하며 중간에 이동 불가능하면 움직이지 않는다
3-0. isdead면 패스, stoned면 stoned 해제하고 패스
3-1. 메두사와의 거리를 줄일 수 있는 방향을 구한다.
3-1-1. 네 방향 으로 한 칸 이동, 해당 칸에서 맨해튼 거리를 메두사와 구하고 구하는 방향 
3-1-2. 최단거리는 아니고 거리를 줄일 수 있는 방향이기 때문에 메두사의 방향 구하는 함수와 달라야 한다
3-2. globalDirBoard에서 0이상이어도 이동 못함을 주의 
3-3. 이동하고 두 번 반복
3-3-1. 전사가 이동한다면 globalwarmovecnt 갱신
3-4. 이동해서 결국 메두사와 같은 칸에 들어가면 사망처리한다(flag 설정)
 */
class Main
{
	static BufferedReader br;
	static BufferedWriter bw;
	static StringTokenizer st;
	
	static int[] MMoveDR = {-1, 1, 0, 0}; // 상하좌우
	static int[] MMoveDC = {0, 0, -1, 1};
	// 상, 상우, 우, 하우, 하, 하좌, 좌, 좌상
	// 짝수 = 4방향, 홀수 = 대각선
	static int[] MSeeDR = {-1, -1, 0, 1, 1, 1, 0, -1};
	static int[] MSeeDC = {0, 1, 1, 1, 0, -1, -1, -1};
	

	
	static int HIDDEN = -1;
	
	/*
	메두사 : 객체
	전사의 이동
	전사:객체 : r, c, idx, isDead
	warExists: List<widx>[r][c] 보드판 - 생명주기 : 턴마다 초기화, 전사 객체 순회하면서 보드판 좌표 채우자
	좌표 갱신도 처리해줘야 함
	globalDirBoard: int[N][N] 보드판 - 생명주기 : 매 턴에 초기화
	globalWarMoveCnt: 전사 전체 이동 횟수
	globalStoneCnt: 메두사에 의해 돌이 된 전사 수
	globalWarHitCnt: 전사가 메두사 친 횟수 (메두사가 죽인건 아님)
	*/
	static class Warrior{
		int r, c, idx;
		boolean isDead;
		boolean stoned;
		Warrior(int r, int c, int idx){
			this.r=r;this.c=c;this.idx=idx;
			this.isDead=false; this.stoned = false;
		}
	}
	static class Medusa{
		int r, c;
		Medusa(int r, int c){
			this.r=r;this.c=c;
		}
	}
	static List<Integer>[][] warExists;
	static int[][] globalDirBoard;
	static int globalWarMoveCnt;
	static int globalStoneCnt;
	static int globalWarHitCnt;
	
	static int TOWNSIZE, WARCNT;
	static int[][] map;
	static int parkr, parkc;
	
	static List<Warrior> warriors;
	static Medusa medusa;
	
	static void init() throws IOException {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        StringTokenizer st = new StringTokenizer(br.readLine().trim());
        TOWNSIZE = Integer.parseInt(st.nextToken());
        WARCNT = Integer.parseInt(st.nextToken());

        st = new StringTokenizer(br.readLine().trim());
        int mr = Integer.parseInt(st.nextToken());
        int mc = Integer.parseInt(st.nextToken());
        medusa = new Medusa(mr, mc);

        parkr = Integer.parseInt(st.nextToken());
        parkc = Integer.parseInt(st.nextToken());

        warExists = new ArrayList[TOWNSIZE][TOWNSIZE];
        globalDirBoard = new int[TOWNSIZE][TOWNSIZE];
        globalWarMoveCnt = 0;
        globalStoneCnt = 0;
        globalWarHitCnt = 0;
        map = new int[TOWNSIZE][TOWNSIZE];
        warriors = new ArrayList<>();

        for(int r = 0; r < TOWNSIZE; r++) {
            for(int c = 0; c < TOWNSIZE; c++) {
                warExists[r][c] = new ArrayList<>();
            }
        }

        st = new StringTokenizer(br.readLine().trim());
        for(int man = 0; man < WARCNT; man++) {
            int r = Integer.parseInt(st.nextToken());
            int c = Integer.parseInt(st.nextToken());
            warriors.add(new Warrior(r, c, man));
        }

        for(int r = 0; r < TOWNSIZE; r++) {
            st = new StringTokenizer(br.readLine().trim());
            for(int c = 0; c < TOWNSIZE; c++) {
                map[r][c] = Integer.parseInt(st.nextToken());
            }
        }
    }

	static int calcDist(int fr, int fc, int tr, int tc) {
		return Math.abs(fr-tr) + Math.abs(fc-tc);
	}
	
	public static void main(String args[]) throws Exception
	{
		// System.setIn(new FileInputStream("res/input.txt"));
		br = new BufferedReader(new InputStreamReader(System.in));
		bw = new BufferedWriter(new OutputStreamWriter(System.out));
			init();

			simulate();
	}
	
	static void printMedusa() {
		System.out.println("메두사 위치 :");
		System.out.println(medusa.r+" , "+medusa.c);
	}
	
	static void printGlobalDirBoard() {
		for(int r=0; r<TOWNSIZE; r++) {
			for(int c=0; c<TOWNSIZE; c++) {
				System.out.print(globalDirBoard[r][c]+" ");
			}
			System.out.println();
		}
	}
	static void printWarExists() {
		for(int r=0; r<TOWNSIZE; r++) {
			for(int c=0; c<TOWNSIZE; c++) {
				System.out.print(warExists[r][c].size());
			}
			System.out.println();
		}
	}
	
	static void simulate() throws IOException {
		for(int turn=0; turn < TOWNSIZE*TOWNSIZE+5; turn++) {
			globalStoneCnt=0;
			globalWarHitCnt=0;
			globalWarMoveCnt=0;
			
			initWarExists();
			
			// printWarExists();
			
			if(!moveMedusa()) {
				bw.write(-1+"\n");
				break;
			}
			if(medusa.r == parkr && medusa.c == parkc) {
				bw.write(0+"\n");
				break;
			}
			
			// printMedusa();
			
			watchMedusa();
			
			// printGlobalDirBoard();
			
			moveWarrior();
			
			StringBuilder sb = new StringBuilder();
			sb.append(globalWarMoveCnt).append(" ").append(globalStoneCnt)
			.append(" ").append(globalWarHitCnt).append("\n");
			bw.write(sb.toString());
		}
		bw.flush();
		bw.close();
	}
	
	static int[] WFirstDR = {-1, 1, 0, 0};
	static int[] WFirstDC = {0, 0, -1, 1};
	static int[] WSecondDR = {0, 0, -1, 1};
	static int[] WSecondDC = {-1, 1, 0, 0};
	static int[] getWarriorPos(int r, int c, int turn) {
		int aptDir = -1;
		int aptDist = calcDist(r, c, medusa.r, medusa.c); // 현재 거리로 해야 함
		int aptR = r;
		int aptC = c;
		for(int dir=0; dir<4; dir++) {
			int nr = r;
			int nc = c;
			if(turn == 0) {
				nr = r + WFirstDR[dir];
				nc = c + WFirstDC[dir];
			}else {
				nr = r + WSecondDR[dir];
				nc = c + WSecondDC[dir];
			}

			
			if(outOfBorder(nr, nc)) continue;
			if(globalDirBoard[nr][nc] >= 0) continue;
			
			int localDist = calcDist(nr, nc, medusa.r, medusa.c);
			if(aptDist > localDist) {
				aptDist = localDist;
				aptDir = dir;
				aptR = nr;
				aptC = nc;
			}
		}
		return new int[] {aptR,aptC, aptDir};
	}
	
	/*
	3. 전사들의 이동 - moveWarrior
	최대 두 칸 이동가능하며 중간에 이동 불가능하면 움직이지 않는다
	전사들은 도로와 도로가 아닌 곳 다 갈 수 있다
	3-1. 메두사와의 거리를 줄일 수 있는 방향을 구한다.
	3-1-1. 네 방향 으로 한 칸 이동, 해당 칸에서 맨해튼 거리를 메두사와 구하고 구하는 방향 
	3-1-2. 최단거리는 아니고 거리를 줄일 수 있는 방향이기 때문에 메두사의 방향 구하는 함수와 달라야 한다
	3-2. globalDirBoard에서 0이상이어도 이동 못함을 주의 
	3-3. 이동하고 두 번 반복
	3-3-1. 전사가 이동한다면 globalwarmovecnt 갱신
	3-4. 이동해서 결국 메두사와 같은 칸에 들어가면 사망처리한다(flag 설정)
	*/
	static void moveWarrior() {
		for(Warrior warrior : warriors) {
			if(warrior.isDead) continue;
			if(warrior.stoned) {
				warrior.stoned = false;
				continue;
			}
			
			// 첫번째 이동
			int[] nextPos = getWarriorPos(warrior.r, warrior.c, 0);	
			if(nextPos[2] == -1) { // 이동 못하면
				continue;
			}
			// 상태 갱신
			warrior.r = nextPos[0];
			warrior.c = nextPos[1];
			// 최종 카운팅 추가
			globalWarMoveCnt++;
			// 사망 처리
			if(warrior.r == medusa.r && warrior.c == medusa.c) {
				warrior.isDead = true;
				globalWarHitCnt++;
				continue;
			}

			
			// 두 번째 이동
			// 첫번째 이동
			nextPos = getWarriorPos(warrior.r, warrior.c, 1);	
			if(nextPos[2] == -1) { // 이동 못하면
				continue;
			}
			// 상태 갱신
			warrior.r = nextPos[0];
			warrior.c = nextPos[1];
			// 최종 카운팅 추가
			globalWarMoveCnt++;
			// 사망 처리
			if(warrior.r == medusa.r && warrior.c == medusa.c) {
				warrior.isDead = true;
				globalWarHitCnt++;
				continue;
			}
		}
	}
	
	/*
	2. 메두사의 시선 - watchMedusa
	2-0-0. candDirBoard[4][N][N]: 네 방향 dirBoard를 저장하는 보드판
	2-0-1. stoneCnt[4] : 네 방향의 돌 수를 저장하는 배열
	2-0. 상,하,좌,우 네 방향으로 본다
	2-1. dirBoard를 만든다 초기 -1(HIDDEN)로 채운다
	2-2. 메두사의 시선 방향에 따라 다음 세칸을 특정 방향으로 채운다
	2-3. 세 칸에 대해서 각 칸들에 해당하는 특정 방향들로만 bfs한다, 그 세 칸도 채운다
	2-3-1. 메두사가 하단 방향 보면, (1,-1):좌하단 대각 + 하단, (1,0): 하단, (1,1):우하단 대각 + 하단
	2-4. dirBoard에 특정방향들을 저장한다
	*/
	static int[][] propagateStone(int OR, int OC, int[] dir, int Mdir, 
			int[][] dirBoard, boolean isWarrior) {
		
			int[][] poses = new int[dir.length][2];
			for(int idx=0; idx<dir.length; idx++) {
				poses[idx] = new int[] {
						OR + MSeeDR[dir[idx]], OC + MSeeDC[dir[idx]]
				};
			};
			Queue<int[]> q = new ArrayDeque<>();
			
			for(int idx=0; idx<poses.length; idx++) {
				if(!outOfBorder(poses[idx][0], poses[idx][1])) {
					if(isWarrior) {
						dirBoard[poses[idx][0]][poses[idx][1]] = HIDDEN;
					}else {
						dirBoard[poses[idx][0]][poses[idx][1]] = dir[idx];
					}
					q.offer(new int[] {poses[idx][0], poses[idx][1], dir[idx]});
				}
			};
			
			while(!q.isEmpty()) {
				int[] now = q.poll();
				int nowr = now[0];
				int nowc = now[1];
				int nowdir = now[2];
				
				int nr = nowr + MSeeDR[nowdir];
				int nc = nowc + MSeeDC[nowdir];
				
				if(!outOfBorder(nr, nc)) {
					if(isWarrior) {
						dirBoard[nr][nc] = HIDDEN;
					}else {
						dirBoard[nr][nc] = nowdir;
					}
					q.offer(new int[] {nr, nc, nowdir});
				}
				
				if(nowdir != Mdir) {
					nr = nowr + MSeeDR[Mdir];
					nc = nowc + MSeeDC[Mdir];
					
					if(!outOfBorder(nr, nc)) {
						if(isWarrior) {
							dirBoard[nr][nc] = HIDDEN;
						}else {
							dirBoard[nr][nc] = nowdir;
						}
						dirBoard[nr][nc] = Mdir;
						q.offer(new int[] {nr,nc, Mdir});
					}
				}
			}
			
			return dirBoard;
	}
	
	static void printBoard(String name, int[][] board) {
		System.out.println(name+":");
		for(int r=0; r<TOWNSIZE; r++) {
			for(int c=0; c<TOWNSIZE; c++) {
				System.out.print(board[r][c]+" ");
			}
			System.out.println();
		}
		System.out.println();
	}
	
	/*
	2-5. dirBoard를 순회하면서 dirBoard가 0이상이면(메두사 시선방향)
	2-6. warExists.size가 0보다 큰지 확인, 전사 존재하면 dirBoard의 방향 + 직진 방향으로 bfs
	2-6-1. bfs하면서 dirBoard를 -1(HIDDEN)로 채운다
	2-7. 다시 dirBoard를 순회하면서 dirBoard가 0이상이면서 warExists가 0보다 큰 수를 카운트한다 
	2-8. 가장 많은 카운팅 된 방향으로 globalDirBoard를 candDirBoard[방향] 으로 초기화한다
	2-9. globalStoneCnt를 stoneCnt[방향] 누적한다
	*/
	static void watchMedusa() {
		int[][][] candDirBoard = new int[4][TOWNSIZE][TOWNSIZE];
		int[] stoneCnt = new int[4];
		List<Integer>[] stonedWarriors = new ArrayList[4];
		for(int i=0; i<4; i++) {
			stonedWarriors[i] = new ArrayList<>();
		}

		// 상 하 좌 우
		int[] dirs = new int[] {0, 4, 6, 2};
		for(int didx=0; didx<4; didx++) {
			int[][] dirBoard = new int[TOWNSIZE][TOWNSIZE];
			for(int r=0; r<TOWNSIZE; r++) {
				Arrays.fill(dirBoard[r], HIDDEN);
			}
			
			int leftDir = (dirs[didx]-1+8)%8;
			int rightDir = (dirs[didx]+1+8)%8;
			
			// 메두사 기준: 해당 dir 방향으로 메두사의 시선을 전파해서 dirBoard를 갱신한다
			dirBoard = propagateStone(medusa.r, medusa.c,
					new int[] {leftDir, dirs[didx], rightDir}, 
					dirs[didx], dirBoard, false);
			
			// printBoard("dirboard 전사 처리 전", dirBoard);
			
			//printWarExists();
			// 전사 기준으로 dirboard 갱신, 결국 가장 앞쪽의 전사만 dirboard 에 남게 됨
			for(int r=0; r<TOWNSIZE; r++) {
				for(int c=0; c<TOWNSIZE; c++) {
					if(dirBoard[r][c] >= 0) {
						if(warExists[r][c].size() > 0) {
							dirBoard = propagateStone(r,c, 
									new int[] {dirBoard[r][c], dirs[didx]},
									dirs[didx], dirBoard, true);
						}
					}
				}
			}
			
			// printBoard("dirboard 전사 처리 후", dirBoard);
			
			// 2-7 개수 체크
			for(int r=0; r<TOWNSIZE; r++) {
				for(int c=0; c<TOWNSIZE; c++) {
					if(dirBoard[r][c] >= 0) {
						if(warExists[r][c].size() > 0) {
							stoneCnt[didx] += warExists[r][c].size();
						}
					}
				}
			}
			
			candDirBoard[didx] = dirBoard;
		}
		
		int maxDir = 0;
		int maxStone = 0;
		for(int dir=0; dir<4; dir++) {
//			 System.out.println(dir+"방향");
//			 System.out.println(stoneCnt[dir]);
			if(maxStone < stoneCnt[dir]) {
				maxStone = stoneCnt[dir];
				maxDir = dir;
			}
		}
		
//		2-8. 가장 많은 카운팅 된 방향으로 globalDirBoard를 candDirBoard[방향] 으로 초기화한다
//		2-9. globalStoneCnt를 stoneCnt[방향] 누적한다
		globalDirBoard = candDirBoard[maxDir];
		globalStoneCnt += stoneCnt[maxDir];
		
		// 2-8-1 개수 체크
		for(int r=0; r<TOWNSIZE; r++) {
			for(int c=0; c<TOWNSIZE; c++) {
				if(globalDirBoard[r][c] >= 0) {
					if(warExists[r][c].size() > 0) {
						// 해당 전사들 stoned 체크
						for(int widx : warExists[r][c]) {
							warriors.get(widx).stoned = true;
						}
					}
				}
			}
		}
	}
	
	/*
	0. warExists 초기화
	0-1. 전사 객체 순회하면서 보드판 좌표를 채운다
	*/
	static void initWarExists() {
		warExists = new ArrayList[TOWNSIZE][TOWNSIZE];
		for(int r=0; r<TOWNSIZE; r++) {
			for(int c=0; c<TOWNSIZE; c++) {
				warExists[r][c] = new ArrayList<>();
			}
		}
		
		for(Warrior warrior : warriors) {
			if(warrior.isDead) continue;
			int r = warrior.r;
			int c = warrior.c;
			
			warExists[r][c].add(warrior.idx);
		}
	}
	
	static boolean outOfBorder(int r, int c) {
		return r<0||c<0||r>=TOWNSIZE||c>=TOWNSIZE;
	}
	
	static int getMedusaDist(int r, int c) {
		Queue<int[]> q = new ArrayDeque<>();
		q.offer(new int[] {r,c});
		int[][] vis = new int[TOWNSIZE][TOWNSIZE];
		vis[r][c] = 1;
		
		while(!q.isEmpty()) {
			int[] now = q.poll();
			int nowr = now[0];
			int nowc = now[1];
			
			if(nowr == parkr && nowc == parkc) break;
			
			for(int dir=0; dir<4; dir++) {
				int nr = nowr + MMoveDR[dir];
				int nc = nowc + MMoveDC[dir];
				
				if(outOfBorder(nr, nc)) continue;
				if(map[nr][nc] == 1) continue;
				if(vis[nr][nc] != 0) continue;

				vis[nr][nc] = vis[nowr][nowc] + 1;
				q.offer(new int[] {nr,nc});
			}
		}
		
		return vis[parkr][parkc];
	}
	
	static int[] getMedusaPos(int r, int c) {
		// System.out.println("getMedusaPos");
		int aptDir = -1;
		int aptDist = TOWNSIZE * TOWNSIZE + 5;
		int aptR = r;
		int aptC = c;
		for(int dir=0; dir<4; dir++) {
			int nr = r + MMoveDR[dir];
			int nc = c + MMoveDC[dir];
			
			if(outOfBorder(nr, nc)) continue;
			if(map[nr][nc] == 1) continue;
			
			int localDist = getMedusaDist(nr, nc);
			if(aptDist > localDist) {
				aptDist = localDist;
				aptDir = dir;
				aptR = nr;
				aptC = nc;
			}
		}
		
		// System.out.println("aptD :"+aptDist+", "+aptDir);
		return new int[] {aptR,aptC, aptDir};
	}
	
	/*
	1. 메두사의 이동 - moveMedusa
	1-1. 1로는 이동 불가, 공원까지 최단 거리로 향하는 방향 구하고, 해당 칸으로 이동
	1-1-1. 방향이 없으면, 못가므로 종료
	1-2. 이동시 warExists 해당 좌표에 존재하는 전사 idx를 가져와서  싹 다 사망 처리한다
	1-2-1. 해당 좌표의 warExists를 비운다
	*/
	static boolean moveMedusa() {
		int sr = medusa.r;
		int sc = medusa.c;
		
		int[] nextPos = getMedusaPos(sr, sc);
		if(nextPos[2] == -1) {
			return false;
		}
		
		medusa.r = nextPos[0];
		medusa.c = nextPos[1];
		
		for(int r=0; r<TOWNSIZE; r++) {
			for(int c=0; c<TOWNSIZE; c++) {
				if((r == medusa.r && c == medusa.c) && warExists[r][c].size() > 0) {
					List<Integer> warIdxList = warExists[r][c];
					
					for(int widx : warIdxList) {
						warriors.get(widx).isDead = true;
					}
					
					warExists[r][c].clear();
				}
			}
		}
		return true;
	}
}