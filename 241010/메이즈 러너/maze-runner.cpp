#include <iostream>
#include <tuple>
#include <vector>
#include <algorithm>
using namespace std;

struct Man {
	int x, y;
	int dead;
};

int n, m, k;
vector<vector<int>> wtable(11, vector<int>(11));
vector<Man> man(11);
pair<int, int> exitxy;
int dist_sum;

int dx[] = { -1, 1, 0, 0 };
int dy[] = { 0, 0, 1, -1 };

int absn(int a, int b) {
	return a > b ? a - b : b - a;
}

int calc_dist(int fx, int fy, int tx, int ty) {
	return absn(fx, tx) + absn(fy, ty);
}

void move() {
	for (int i = 1; i <= m; i++) {
		if (man[i].dead) continue;

		int x = man[i].x;
		int y = man[i].y;
		int min_dist = 100;
		int min_dir = 100;
		int cur_dist = calc_dist(x, y, exitxy.first, exitxy.second);

		for (int dir = 0; dir < 4; dir++) {
			int nx = x + dx[dir];
			int ny = y + dy[dir];

			if (nx < 1 || ny < 1 || nx > n || ny > n) continue;
			if (wtable[nx][ny] > 0) continue;

			int after_dist = calc_dist(nx, ny, exitxy.first, exitxy.second);

			if (min_dist > after_dist) {
				min_dist = after_dist;
				min_dir = dir;
			}
		}

		if (min_dir != 100 && min_dist < cur_dist) {
			dist_sum += 1;

			int nx = x + dx[min_dir];
			int ny = y + dy[min_dir];
			if (nx == exitxy.first && ny == exitxy.second) {
				man[i].dead = true;
			}
			man[i].x = nx;
			man[i].y = ny;
		}

	}
}

tuple<int, int, int> find_square() {
	for (int size = 2; size <= n; size++) {
		// 범위 시작 왼쪽 위 점
		for (int r = 1; r <= n - size + 1; r++) {
			for (int c = 1; c <= n - size + 1; c++) {
				bool mfound = false;
				bool efound = false;
				// 인간 체크
				for (int i = 1; i <= m; i++) {
					if (man[i].dead) continue;

					if ((r <= man[i].x && man[i].x <= r + size - 1) &&
						(c <= man[i].y && man[i].y <= c + size - 1)) {
						mfound = true;
						break;
					}
				}

				// exit 체크
				if ((r <= exitxy.first && exitxy.first <= r + size - 1) &&
					(c <= exitxy.second && exitxy.second <= c + size - 1)) {
					efound = true;
				}

				if (mfound && efound) {
					return make_tuple(r, c, size);
				}
			}
		}
	}

	// 실패 처리
	return make_tuple(-1, -1, -1);
}

void rotate_man(int x, int y, int size) {

	for (int i = 1; i <= m; i++) {
		if (man[i].dead) continue;
		if ((x <= man[i].x && man[i].x <= x + size - 1) &&
			(y <= man[i].y && man[i].y <= y + size - 1)) {

			int r = man[i].x - x;
			int c = man[i].y - y;

			int nx = c + x;
			int ny = -r + size - 1 + y;

			man[i].x = nx;
			man[i].y = ny;
		}
	}

	if ((x <= exitxy.first && exitxy.first <= x + size - 1) &&
		(y <= exitxy.second && exitxy.second <= y + size - 1)) {

		int r = exitxy.first - x;
		int c = exitxy.second - y;

		int nx = c + x;
		int ny = -r + size - 1 + y;

		exitxy.first = nx;
		exitxy.second = ny;
	}
}

void rotate_wall(int x, int y, int size) {
	vector<vector<int>> tmp = wtable;

	for (int i = x; i <= x + size - 1; i++) {
		for (int j = y; j <= y + size - 1; j++) {
			if (wtable[i][j] > 0) {
				wtable[i][j] -= 1;
			}
		}
	}

	for (int i = x; i <= x + size - 1; i++) {
		for (int j = y; j <= y + size - 1; j++) {

			// (i, j)를 중심 (x, y)에서의 상대 좌표로 변환
			int r = i - x;
			int c = j - y;
			// 시계방향 90도 회전 후 새로운 좌표
			int nx = c + x;
			int ny = -r + size - 1 + y;

			tmp[nx][ny] = wtable[i][j];
		}
	}

	wtable = tmp;
}

void print_wtable() {
	for (int i = 1; i <= n; i++) {
		for (int j = 1; j <= n; j++) {
			cout << wtable[i][j] << ' ';
		}
		cout << '\n';
	}
	cout << '\n';
}

void print_man() {
	for (int i = 1; i <= m; i++) {
		cout << i << " man: " << man[i].x << ' ' << man[i].y << '\n';
	}
	cout << '\n';

}

int main() {
	cin >> n >> m >> k;
	for (int i = 1; i <= n; i++) {
		for (int j = 1; j <= n; j++) {
			cin >> wtable[i][j];
		}
	}
	for (int i = 1; i <= m; i++) {
		int x, y;
		cin >> x >> y;
		man[i].x = x;
		man[i].y = y;
	}
	cin >> exitxy.first >> exitxy.second;

	while (k--) {
		//move
		
		//print_man();
		move();
		//print_man();

		//find_square
		tuple<int, int, int> t = find_square();
		int x = get<0>(t);
		int y = get<1>(t);
		int size = get<2>(t);
		//rotate_man
		rotate_man(x, y, size);

		//print_wtable();
		
		//rotate_wall
		rotate_wall(x, y, size);
		
		//print_wtable();

	}

	cout << dist_sum << '\n';
	cout << exitxy.first << ' ' << exitxy.second;
}