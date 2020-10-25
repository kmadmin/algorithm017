/*
 * @lc app=leetcode.cn id=455 lang=java
 *
 * [455] 分发饼干
 */

// @lc code=start
class Solution {
    public int findContentChildren(int[] g, int[] s) {
        if (null == g || null == s) return 0;
        Arrays.sort(g);
        Arrays.sort(s);
        int gi = 0;
        int si = 0;
        while (gi < g.length && si < s.length) {
            if (g[gi] <= s[si]) {
                gi++;
            } 
            si++;
        }
        return gi;

    }
}
// @lc code=end

