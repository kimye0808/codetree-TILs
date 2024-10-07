#include <iostream>
#include <vector>
#include <algorithm>
using namespace std;

int dx[4] = { 1, 0, -1, 0 };
int dy[4] = { 0, 1, 0, -1 };

pair<int, int> gol[5] = {
    {0,0}, //원점
    {0,1}, //오른쪽
    {-1,0}, //아랫쪽
    {0,-1}, //왼쪽
    {1,0}  //윗쪽
};

bool rotate_gol(pair<int, int> (&gtmp)[5], vector<vector<bool>>& vis, int& exitdir,
    int lr, int r, int c) {
    //1) 왼쪽 or 오른쪽 이동
    for (int i = 0; i < 5; i++) {
        int lrx = gtmp[i].first;
        int lry = gtmp[i].second + lr;//lr 이 -1이면 left, lr이 +1이면 right

        if (lry < 1 || lry > c) {
            return false;
        }

        if (vis[lrx][lry]) {
            return false;
        }
    }

    //2) 아래로 이동
    for (int i = 0; i < 5; i++) {
        int lrx = gtmp[i].first - 1;
        int lry = gtmp[i].second + lr;

        if (lrx < 1 || lry < 1 || lrx > r || lry > c) {
            return false;
        }

        if (vis[lrx][lry]) {
            return false;
        }
    }

    // 좌표 갱신
    for (int i = 0; i < 5; i++) {
        gtmp[i].first = gtmp[i].first - 1;
        gtmp[i].second = gtmp[i].second + lr;
    }
    //반시계 방향
    if (lr == -1) {
        exitdir = (exitdir - 1 + 4) % 4;
    }
    else if(lr== 1){
        exitdir = (exitdir + 1 + 4) % 4;
    }
    return true;
}

bool move_gol(vector<vector<int>>& maps, vector<vector<bool>>& vis, int& sum,
    int r, int c, int col, int d) {

    pair<int, int> gtmp[5] = {
        {0,0},
        {0,1},
        {-1,0},
        {0,-1},
        {1,0}
    };
    int exitdir = d;//0,1,2,3

    for (int i = 0; i < 5; i++) {
        gtmp[i].first = gol[i].first + r + 2;
        gtmp[i].second = gol[i].second + col;
    }

    int rcnt = 0;
    while (1) {
        int rx = gtmp[1].first - 1;
        int ry = gtmp[1].second;
        if (rx < 1) break;
        bool rvis = vis[rx][ry];

        int bx = gtmp[2].first - 1;
        int by = gtmp[2].second;
        if (bx < 1) break;
        bool bvis = vis[bx][by];

        int lx = gtmp[3].first - 1;
        int ly = gtmp[3].second;
        if (lx < 1) break;
        bool lvis = vis[lx][ly];

        if (!lvis && !bvis && !rvis) {//아래로 이동할 수 있으면
            for (int i = 0; i < 5; i++) {
                gtmp[i].first -= 1;
            }
        }
        else if (!rotate_gol(gtmp, vis, exitdir, -1, r, c)) {
            if (!rotate_gol(gtmp, vis, exitdir, 1, r, c)) {
                break;
            }
        }
    }//while(1)-이동 끝

    if (gtmp[4].first > r) {//숲에 다 못들어갔다면
        return false;
    }

    //정상작동
    int x = gtmp[exitdir].first;
    int y = gtmp[exitdir].second;
    int lowr = gtmp[2].first;

    // 붙어있는 블록 확인
    for (int i = 0; i < 4; i++) {
        int nx = x + dx[i];
        int ny = y + dy[i];

        if (nx<1 || ny<1 || nx>r || ny>c) continue;

        if (vis[nx][ny]) {
            lowr = min(lowr, maps[nx][ny]);
        }
    }

    for (int i = 0; i < 5; i++) {
        int x = gtmp[i].first;
        int y = gtmp[i].second;

        vis[x][y] = true;
        maps[x][y] = lowr;//맨 밑 row값을 저장
    }
    sum += r + 1 - lowr;//1행이면 r행
    return true;
}


int main() {
    ios::sync_with_stdio(0); cin.tie(0); cout.tie(0);
    vector<vector<int>> maps(100, vector<int>(100, 0));
    vector<vector<bool>> vis(100, vector<bool>(100, false));

    int r, c, k;
    cin >> r >> c >> k;
    int sum = 0;

    for (int i = 0; i < k; i++) {
        int col, d;
        cin >> col >> d;
        if (!move_gol(maps, vis, sum, r, c, col, d)) {
            for (int j = 0; j < 100; j++) {
                fill(maps[j].begin(), maps[j].end(), 0);
                fill(vis[j].begin(), vis[j].end(), false);
            }
        }
    }

    cout << sum;

    return 0;
}