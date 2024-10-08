#include <iostream>
#include <vector>
#include <algorithm>
#include <queue>
using namespace std;

int k, m;
vector<vector<int>> table(6, vector<int>(6));
queue<int> wall;
int dx[] = { 1, 0, -1, 0 };
int dy[] = { 0, 1, 0, -1 };

vector<vector<int>> rot_degree_first(int cx, int cy, int rot) {
	vector<vector<int>> tmp_table(6, vector<int>(6));
	for (int i = 1; i <= 5; i++) {
		for (int j = 1; j <= 5; j++) {
			tmp_table[i][j] = table[i][j];
		}
	}

	for (int i = cx - 1; i <= cx + 1; i++) {
		for (int j = cy - 1; j <= cy + 1; j++) {
			int ox = i - cx;
			int oy = j - cy;
			if (rot == 90) {
				tmp_table[ox + cx][oy + cy] = table[-oy + cx][ox + cy];
			}
			else if (rot == 180) {
				tmp_table[ox + cx][oy + cy] = table[-ox + cx][-oy + cy];
			}
			else if (rot == 270) {
				tmp_table[ox + cx][oy + cy] = table[oy + cx][-ox + cy];
			}
		}
	}
	
	return tmp_table;
}

void rot_degree_cons(int cx, int cy, int rot) {
	vector<vector<int>> tmp_table(6, vector<int>(6));
	for (int i = 1; i <= 5; i++) {
		for (int j = 1; j <= 5; j++) {
			tmp_table[i][j] = table[i][j];
		}
	}

	for (int i = cx - 1; i <= cx + 1; i++) {
		for (int j = cy - 1; j <= cy + 1; j++) {
			int ox = i - cx;
			int oy = j - cy;
			if (rot == 90) {
				tmp_table[ox + cx][oy + cy] = table[-oy + cx][ox + cy];
			}
			else if (rot == 180) {
				tmp_table[ox + cx][oy + cy] = table[-ox + cx][-oy + cy];
			}
			else if (rot == 270) {
				tmp_table[ox + cx][oy + cy] = table[oy + cx][-ox + cy];
			}
		}
	}

	for (int i = 1; i <= 5; i++) {
		for (int j = 1; j <= 5; j++) {
			table[i][j] = tmp_table[i][j];
		}
	}
}

int bfs_first(int a, int b, vector<vector<bool>>& vis, vector<vector<int>>& tmp_table) {
	queue<pair<int, int>> q;
	q.push(make_pair(a, b));
	vis[a][b] = true;

	int ymcnt = 0;

	while (!q.empty()) {
		pair<int,int> p = q.front();
		int x = p.first;
		int y = p.second;
		q.pop();

		ymcnt++;

		for (int dir = 0; dir < 4; dir++) {
			int nx = x + dx[dir];
			int ny = y + dy[dir];

			if (nx < 1 || ny < 1 || nx>5 || ny>5) continue;
			if (vis[nx][ny]) continue;
			if (tmp_table[x][y] != tmp_table[nx][ny]) continue;

			vis[nx][ny] = true;
			q.push(make_pair(nx, ny));
		}
	}

	//cout <<"ymcnt: "<< ymcnt << '\n';
	ymcnt = ymcnt >= 3 ? ymcnt : 0;
	return ymcnt;
}

int bfs_cons(int a, int b, vector<vector<bool>>& vis) {
	queue<pair<int, int>> q;
	queue<pair<int, int>> cand;// 삭제할 후보군
	q.push(make_pair(a, b));
	cand.push(make_pair(a, b));
	vis[a][b] = true;

	int ymcnt = 0;

	while (!q.empty()) {
		pair<int, int> p = q.front();
		int x = p.first;
		int y = p.second;
		q.pop();

		ymcnt++;

		for (int dir = 0; dir < 4; dir++) {
			int nx = x + dx[dir];
			int ny = y + dy[dir];

			if (nx < 1 || ny < 1 || nx>5 || ny>5) continue;
			if (vis[nx][ny]) continue;
			if (table[x][y] != table[nx][ny]) continue;

			vis[nx][ny] = true;
			q.push(make_pair(nx, ny));
			cand.push(make_pair(nx, ny));
		}
	}

	// 유물이면 해당 유물 자리를 0으로 만듦
	if (ymcnt >= 3) {
		while (!cand.empty()) {
			int x = cand.front().first;
			int y = cand.front().second;
			cand.pop();

			table[x][y] = 0;
		}
	}
	else {
		ymcnt = 0;
	}

	return ymcnt;
}


int count_ym_first(vector<vector<int>>& tmp_table) {
	vector<vector<bool>> vis(6, vector<bool>(6, false));

	int ym_sum = 0;
	for (int i = 1; i <= 5; i++) {
		for (int j = 1; j <= 5; j++) {
			if (!vis[i][j]) {
				ym_sum += bfs_first(i, j, vis, tmp_table);
			}
		}
	}

	return ym_sum;
}


int count_ym_cons() {
	vector<vector<bool>> vis(6, vector<bool>(6, false));

	int ym_sum = 0;
	for (int i = 1; i <= 5; i++) {
		for (int j = 1; j <= 5; j++) {
			if (!vis[i][j]) {
				ym_sum += bfs_cons(i, j, vis);
			}
		}
	}

	return ym_sum;
}


bool fill_table() {
	for (int i = 1; i <= 5; i++) {// 열 1부터 5
		for (int j = 5; j >= 1; j--) {// 행 5부터 1
			if (table[j][i] == 0) {
				if (!wall.empty()) {
					int w = wall.front();
					wall.pop();

					table[j][i] = w;
				}
				else {
					return false;
				}
			}
		}
	}
	return true;
}


int main() {
	cin >> k >> m;

	for (int i = 1; i <= 5; i++) {
		for (int j = 1; j <= 5; j++) {
			cin >> table[i][j];
		}
	}
	for (int i = 1; i <= m; i++) {
		int tmp;
		cin >> tmp;
		wall.push(tmp);
	}

	for (int i = 0; i < k; i++) {
		// 유믈 1차 획득은 임시 테이블로 가장 큰 좌표와 각도 설정
		// 중심 좌표 선택
		pair<int, int> maxc;
		int max_fcnt = 0;
		int max_dg = 0;
		for (int dg = 90; dg <= 270; dg += 90) {
			for (int cy = 2; cy <= 4; cy++) {
				for (int cx = 2; cx <= 4; cx++) {
					vector<vector<int>> tmp_table = rot_degree_first(cx, cy, dg);
					int first_cnt = count_ym_first(tmp_table);

					if (first_cnt > max_fcnt) {
						max_fcnt = first_cnt;
						maxc = make_pair(cx, cy);
						max_dg = dg;
					}
				}
			}
		}
		// 유물 자체를 찾을 수 없으면 종료
		if (max_fcnt == 0) {
			break;
		}
		// 연속 획득은 진짜 테이블로 연속 카운팅
		// 선택된 격자, 선택된 회전각으로 연쇄 카운팅
		int max_cx = maxc.first;
		int max_cy = maxc.second;

		int cons_sum = 0;

		rot_degree_cons(max_cx, max_cy, max_dg);
		while (1) {
			int local_sum = count_ym_cons();
			if (local_sum == 0) {// 유물 획득 불가시 종료
				break;
			}

			cons_sum += local_sum;

			// 비어있는 tmp_table을 wall에서 채워넣음
			if (!fill_table()) {
				return 0;
			}

			//cout << '\n';
			//for (int j = 1; j <= 5; j++) {
			//	for (int k = 1; k <= 5; k++) {
			//		cout << table[j][k] << ' ';
			//	}
			//	cout << '\n';
			//}
		}
		cout << cons_sum<<' ';

	}
}