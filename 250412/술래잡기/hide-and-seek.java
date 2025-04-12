import java.io.*;
import java.util.*;

/*
 * 거리 3 이하만 이동 가능 조건 구현을 빼먹어서 오래 걸림
 * 조건 빼먹었지는 않았는지 체크하자
 * 벽 충돌시 반대 방향을 객체 상태에 갱신 안해줘서 이것도 오래걸림
 */
/*
 * N 보드크기
 * M 도망자 수
 * H 나무 수
 * K 턴 반복
 * 거리 = 맨해튼
 * 도망자-도망자, 도망자-나무 는 겹칠 수 있다
 * 
 * Runner 도망자=객체: x, y, dir, moveKind:(1:좌우, 2:상하), isDead
 * 좌우면 항상 오른쪽 보고 시작, 상하면 항상 아래쪽 보고 시작
 * 나무 = 보드판
 * Catcher 술래 = 객체: x, y, dir, 초기:3, isOut(out으로 나가는 중이냐)
 * 
 * 방향 = 0: 하, 1: 우, 2: 좌, 3: 상
 * 3-dir = 반대방향
 * 
 * Runner[]
 * Catcher
 * inDirBoard
 * outDirBoard
 * runnerMap:HashMap<좌표string, Set<idx>>
 * 
 * 1. moveRunner
 * 1-0. canMove
 * 1-1. dir로 한 칸 이동 가능한 nx, ny
 * 1-2. outOfBorder 체크
 * 1-2-1. outOfBorder면 dir을 3-dir
 * 1-2-2. nx, ny를 다시 계산, 술래 좌표 체크, 술래 좌표랑 다르면 이동
 * 1-3. 술래 있으면 이동 X
 * 1-4. 해당 좌표 runnerMap에 idx추가, 기존 좌표 runnerMap에서 idx 삭제
 * 
 * 2. makeOutDirBoard
 * 2-0. 달팽이 방향으로 이동, 바깥으로 나가면서 보드판에 방향 갱신
 * 2-0. 이동하기 전에 이동할 방향 기록하고 나감
 * 2-2. 좌표가 1,1 일때까지 반복
 * 2-2-1. 방향 유지 거리 크기는 패턴 2번 유지
 * 2-2-2. 방향은 방향 유지 거리 크기만큼 유지
 * 2-3. 반복, 1,1이 되면 방향은 하(0)으로 체크하고 끝
 * 
 * 3. makeInDirBoard
 * 2-0. 달팽이 방향
 * 2-0. 1,1에서 하(0)부터 시작
 * 2-1. outDirBoard와 유사, 거리 크기만 N-1로 세 번 처리
 * 2-2. 중앙에 올때까지 처리
 * 2-3. 중앙 N/2+1, N/2+1 에선 방향 상(3) 처리하고 끝
 * 
 * 2. moveCatcher
 * 2-0. 양 끝에 도달하면 flag 바꿔야 함
 * 2-0. 방향 판을 미리 만들어 둬야 함
 * 2-1. isOut 체크, 방향에 따라 해당 방향보드판 참조해야 함
 * 2-2. 객체의 dir로 이동하고, 방향보드판으로 객체 dir 갱신
 * 2-3. catchRunner 수행
 * 
 * 3. catchRunner
 * 3-1. 술래 위치에서 술래 dir로 술래칸 포함 총3칸 체크
 * 3-2. 나무칸이 아니면, runnerMap으로 좌표별 도망자set 체크,
 * 3-2-1. 도망자 set 돌면서 dead 처리, 해당 좌표 삭제
 */
class Main {
	public static void main(String args[]) throws Exception {
		Scanner sc = new Scanner(System.in);

		init(sc);

		simulate();

		System.out.println(score);
	}

	static int[] DX = { 1, 0, 0, -1 }; // 하 우 좌 상
	static int[] DY = { 0, 1, -1, 0 };

	static class Runner {
		int x, y, kind, dir;
		boolean isDead;

		Runner(int x, int y, int kind) {
			this.x = x;
			this.y = y;
			this.kind = kind;
			if (kind == 1) {
				this.dir = 1;
			} else if (kind == 2) {
				this.dir = 0;
			} else {
				System.out.println("runner 방향 설정 에러");
			}
			isDead = false;
		}
	}

	static class Catcher {
		int x, y, dir;
		boolean isOut;

		Catcher(int x, int y, int dir) {
			this.x = x;
			this.y = y;
			this.dir = dir;
			this.isOut = true;
		}
	}

	static int N, M, H, K;
	static Runner[] runners; // 1-based
	static Catcher catcher;
	static boolean[][] treeBoard;
	static int[][] inDirBoard, outDirBoard;
	static Map<String, Set<Integer>> runnerMap;
	static int score;

	static void init(Scanner sc) {
		N = sc.nextInt(); // 칸 사이즈
		M = sc.nextInt(); // 도망자 수
		H = sc.nextInt(); // 나무 수
		K = sc.nextInt(); // 턴 반복

		score = 0;
		runners = new Runner[M + 1];
		inDirBoard = new int[N + 1][N + 1];
		outDirBoard = new int[N + 1][N + 1];
		runnerMap = new HashMap<>();
		treeBoard = new boolean[N + 1][N + 1];
		catcher = new Catcher(N / 2 + 1, N / 2 + 1, 3);

		for (int idx = 1; idx <= M; idx++) {
			int x = sc.nextInt();
			int y = sc.nextInt();
			int d = sc.nextInt();
			runners[idx] = new Runner(x, y, d);
			addRunner(x, y, idx);
		}

		for (int idx = 1; idx <= H; idx++) {
			int tx = sc.nextInt();
			int ty = sc.nextInt();

			treeBoard[tx][ty] = true;
		}
	}

	static void addRunner(int nx, int ny, int ridx) {
		Set<Integer> nxySet = runnerMap.get(nx + "," + ny);
		if (nxySet == null) {
			nxySet = new HashSet<>();
		}
		nxySet.add(ridx);
		runnerMap.put(nx + "," + ny, nxySet);
	}

	static void removeRunner(int x, int y, int ridx) {
		Set<Integer> xySet = runnerMap.get(x + "," + y);
		if (xySet == null) {
			System.out.println("이상한 에러 이동 전 맵이 없음");
			return;
		}
		xySet.remove(ridx);
		runnerMap.put(x + "," + y, xySet);
	}

	static void printDirBoard() {
		System.out.println("in");
		for (int r = 1; r <= N; r++) {
			for (int c = 1; c <= N; c++) {
				System.out.print(inDirBoard[r][c]);
			}
			System.out.println();
		}
		System.out.println("out");
		for (int r = 1; r <= N; r++) {
			for (int c = 1; c <= N; c++) {
				System.out.print(outDirBoard[r][c]);
			}
			System.out.println();
		}
	}

	static void printUsers() {
		for (int ridx = 1; ridx <= M; ridx++) {
			if (runners[ridx].isDead)
				continue;
			System.out.print(ridx + " 사용자는 " + runners[ridx].x + " " + runners[ridx].y + " ");
		}
		System.out.println();
	}

	static void simulate() {
		// 방향 판 초기화
		// makeInBoard
		makeInBoard();
		// makeOutBoard
		makeOutBoard();

		for (int turn = 1; turn <= K; turn++) {
			// moveRunner
			moveRunner();
			// moveCatcher
			moveCatcher(turn);

//			printUsers();
//			System.out.println("술래는 "+catcher.x+" "+catcher.y +" 방향:"+catcher.dir);
//			System.out.println("점수 :"+score);
		}
	}

	private static void moveCatcher(int turn) {
		int nowx = catcher.x;
		int nowy = catcher.y;

		if (nowx == 1 && nowy == 1) {
			catcher.isOut = false;
		} else if (nowx == N / 2 + 1 && nowy == N / 2 + 1) {
			catcher.isOut = true;
		}

		int[][] nowDirBoard = null;
		if (catcher.isOut) {
			nowDirBoard = outDirBoard;
		} else {
			nowDirBoard = inDirBoard;
		}

		int nextx = nowx + DX[catcher.dir];
		int nexty = nowy + DY[catcher.dir];
		int newDir = nowDirBoard[nextx][nexty];

		catcher.dir = newDir;
		catcher.x = nextx;
		catcher.y = nexty;

		catchRunner(turn);
	}

	private static void catchRunner(int turn) {
		for (int dist = 0; dist < 3; dist++) {
			int nx = catcher.x + dist * DX[catcher.dir];
			int ny = catcher.y + dist * DY[catcher.dir];

			if (outOfBorder(nx, ny))
				continue;
			if (treeBoard[nx][ny])
				continue;

			Set<Integer> caughtSet = runnerMap.get(nx + "," + ny);
			if (caughtSet == null)
				continue; // 없는 자리
			score += turn * caughtSet.size();
			for (int ridx : caughtSet) {
				runners[ridx].isDead = true;
			}
			runnerMap.remove(nx + "," + ny);
		}
	}

	static boolean canMove(int x, int y) {
		int dist = Math.abs(catcher.x - x) + Math.abs(catcher.y - y);
		if (dist <= 3) {
			return true;
		} else {
			return false;
		}
	}

	private static void moveRunner() {
		for (int ridx = 1; ridx <= M; ridx++) {
			if (runners[ridx].isDead)
				continue;

			int x = runners[ridx].x;
			int y = runners[ridx].y;
			if (!canMove(x, y))
				continue;
			int dir = runners[ridx].dir;

			int nx = x + DX[dir];
			int ny = y + DY[dir];

			if (outOfBorder(nx, ny)) {
				dir = 3 - dir;
				nx = x + DX[dir];
				ny = y + DY[dir];
			}

			if (nx == catcher.x && ny == catcher.y) {
				continue; // 이동 X
			}

			runners[ridx].x = nx;
			runners[ridx].y = ny;
			runners[ridx].dir = dir;

			// 맵 갱신
			addRunner(nx, ny, ridx);

			removeRunner(x, y, ridx);
		}
	}

	static boolean outOfBorder(int x, int y) {
		return x < 1 || y < 1 || x > N || y > N;
	}

	private static void makeOutBoard() {
		int sr = N / 2 + 1;
		int sc = N / 2 + 1;

		// 방향 = 하 , 우 , 좌, 상
		// 3 1 0 2 순으로 진행해야함
		int[] DIR = { 3, 1, 0, 2 };
		int nr = sr;
		int nc = sc;
		int SIZE = 1;
		int dir = 0;
		outDirBoard[nr][nc] = 3;
		while (true) {
			for (int patternCnt = 0; patternCnt < 2; patternCnt++) {
				for (int size = 1; size <= SIZE; size++) {
					int pr = nr;
					int pc = nc;

					nr += DX[DIR[dir]];
					nc += DY[DIR[dir]];

					outDirBoard[pr][pc] = DIR[dir];

					if (nr == 1 && nc == 1) {
						outDirBoard[nr][nc] = 0;
						return;
					}
				}
				dir = (dir + 1) % 4;
			}
			SIZE++;
		}
	}

	private static void makeInBoard() {
		int sr = 1;
		int sc = 1;

		// 방향 = 하 , 우 , 좌, 상
		// 0 1 3 2 순으로 진행해야함
		// 인데 먼저 N-1칸 하 로 진행 하므로 1 3 2 0
		int[] DIR = { 1, 3, 2, 0 };
		int nr = sr;
		int nc = sc;
		int SIZE = N - 1;
		for (int kan = 1; kan <= N - 1; kan++) {
			int pr = nr;
			int pc = nc;

			nr += DX[DIR[3]];
			nc += DY[DIR[3]];

			inDirBoard[pr][pc] = DIR[3];
		}
		int dir = 0;
		while (true) {
			for (int patternCnt = 0; patternCnt < 2; patternCnt++) {
				for (int size = 1; size <= SIZE; size++) {
					int pr = nr;
					int pc = nc;

					nr += DX[DIR[dir]];
					nc += DY[DIR[dir]];

					inDirBoard[pr][pc] = DIR[dir];

					if (nr == N / 2 + 1 && nc == N / 2 + 1) {
						inDirBoard[nr][nc] = 3;
						return;
					}
				}
				dir = (dir + 1) % 4;
			}
			SIZE--;
		}
	}
}