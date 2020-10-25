/*
 * @lc app=leetcode.cn id=200 lang=java
 *
 * [200] 岛屿数量
 */

// @lc code=start
class Solution {
    public int numIslands(char[][] grid) {
        if (null == grid || 0 == grid.length ) {
            return 0;
        }

        int nr = grid.length;
        int nc = grid[0].length;
        int num_islands = 0;
        for (int r=0; r<nr; r++ ) {
            for (int c=0; c<nc; c++) {
                if ('1'==grid[r][c]) {
                    num_islands++;
                    dfs(grid, r, c);
                }
            }
        }
        return num_islands;
    }

    void dfs(char[][] grid, int r, int c) {
        if (null == grid || r<0 || c<0) {
            return;
        }

        int nr = grid.length;
        int nc = grid[0].length;

        if (r>=nr || c>=nc || '0'==grid[r][c] ) {
            return;
        }

        grid[r][c] = '0';
        dfs(grid, r-1, c);
        dfs(grid, r+1, c);
        dfs(grid, r, c-1);
        dfs(grid, r, c+1);
    }
}
// @lc code=end

