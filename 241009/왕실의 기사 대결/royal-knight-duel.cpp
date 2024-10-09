#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

struct Sol {
	int health;
	int trapcnt;
	bool isdead;
};

int l, n, q;
vector<pair<int, int>> solxy[31];
vector<vector<int>> swtable(41, vector<int>(41));
vector<vector<bool>> ttable(41, vector<bool>(41));
vector<Sol> sol(31);
vector<int> answer(31);//Q 번의 대결이 모두 끝난 후 생존한 기사들이 총 받은 대미지의 합

int dx[] = { -1, 0, 1, 0 };
int dy[] = { 0, 1, 0, -1 };

/*
* si(기사 번호), d(움직일 방향)
*/
bool check_move(int si, int d) {
	vector<bool> millins(31);

	for (pair<int, int> sxy : solxy[si]) {
		int x = sxy.first;
		int y = sxy.second;

		int nx = x + dx[d];
		int ny = y + dy[d];
		
		if (nx < 1 || ny < 1 || nx > l || ny > l) return false;
		if (swtable[nx][ny] == -1) return false;

		if (swtable[nx][ny] > 0 && swtable[nx][ny] != si) {
			millins[swtable[nx][ny]] = true;
		}
	}
	for (int i = 1; i <= n; i++) {
		if (millins[i]) {
			if (!check_move(i, d)) {
				return false;//벽이 있으면
			}
		}
	}
	return true;// 벽이 없을시
}

void kill_sol(int si) {
	for (int i = 1; i <= l; i++) {
		for (int j = 1; j <= l; j++) {
			if (swtable[i][j] == si) {
				swtable[i][j] = 0;
			}
		}
	}
	sol[si].isdead = true;
}

void millin_move(int si, int d) {
	vector<bool> millins(31);
	vector<pair<int, int>> changed;
	int tcnt = 0;

	for (pair<int, int>& sxy : solxy[si]) {
		int x = sxy.first;
		int y = sxy.second;
		swtable[x][y] = 0;

		int nx = x + dx[d];
		int ny = y + dy[d];

		if (swtable[nx][ny] > 0 && swtable[nx][ny] != si) {
			millins[swtable[nx][ny]] = true;
		}
		if (ttable[nx][ny]) {
			tcnt++;
		}
		changed.push_back(make_pair(nx, ny));
	}
	for (int i = 1; i <= n; i++) {
		if (millins[i]) {
			millin_move(i, d);
		}
	}
	
	for (int i = 0; i < (int)changed.size(); i++) {
		swtable[changed[i].first][changed[i].second] = si;
	}

	solxy[si] = changed;

	sol[si].health -= tcnt;
	answer[si] += tcnt;
	if (sol[si].health <= 0) {
		kill_sol(si);
	}
}

void ordered_move(int si, int d) {
	vector<bool> millins(31);
	vector<pair<int, int>> changed;
	int tcnt = 0;

	for (pair<int, int>& sxy : solxy[si]) {
		int x = sxy.first;
		int y = sxy.second;
		swtable[x][y] = 0;

		int nx = x + dx[d];
		int ny = y + dy[d];

		if (swtable[nx][ny] > 0 && swtable[nx][ny] != si) {
			millins[swtable[nx][ny]] = true;
		}
		changed.push_back(make_pair(nx, ny));
	}
	for (int i = 1; i <= n; i++) {
		if (millins[i]) {
			millin_move(i, d);
		}
	}
	for (int i = 0; i < (int)changed.size(); i++) {
		swtable[changed[i].first][changed[i].second] = si;
	}
	solxy[si] = changed;
 }


int main() {
	cin >> l >> n >> q;
	for (int i = 1; i <= l; i++) {
		for (int j = 1; j <= l; j++) {
			int tmp;
			cin >> tmp;
			if (tmp == 2) {
				swtable[i][j] = -1;
			}
			else if (tmp == 1) {
				ttable[i][j] = true;
			}
		}
	}

	// 기사 정보 저장
	for (int i = 1; i <= n; i++) {
		int r, c, h, w, k;
		cin >> r >> c >> h >> w >> k;
		
		sol[i].health = k;

		for (int j = r; j < r + h; j++) {
			for (int z = c; z < c + w; z++) {
				int x = j;
				int y = z;

				solxy[i].push_back(make_pair(x, y));
				
				swtable[x][y] = i;
			}
		}
	}

	//for (int j = 1; j <= l; j++) {
	//	for (int k = 1; k <= l; k++) {
	//		cout << swtable[j][k] << ' ';
	//	}
	//	cout << '\n';
	//}

	// 명령 진행
	for (int order = 1; order <= q; order++) {
		int i, d;
		cin >> i >> d;

		//check_move
		if (!check_move(i, d)) {
			continue;//어차피 다음 한 칸이 벽 존재, 이동 불가
		}

		if (!sol[i].isdead) {
			// ordered_move
			ordered_move(i, d);

			//for (int j = 1; j <= l; j++) {
			//	for (int k = 1; k <= l; k++) {
			//		cout << swtable[j][k] << ' ';
			//	}
			//	cout << '\n';
			//}
		}
	}

	int result = 0;
	for (int i = 1; i <= n; i++) {
		if (sol[i].isdead == false) {
			result += answer[i];
		}
	}
	cout << result;
}