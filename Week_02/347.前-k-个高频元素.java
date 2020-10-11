import java.util.Map;
import java.util.PriorityQueue;

/*
 * @lc app=leetcode.cn id=347 lang=java
 *
 * [347] 前 K 个高频元素
 */

// @lc code=start
class Solution {
    public int[] topKFrequent(int[] nums, int k) {
        Map<Integer, Integer> occurrences = new HashMap<Integer, Integer>();
        for (int num: nums) {
            occurrences.put(num, occurrences.getOrDefault(num, 0) +1);
        }

        PriorityQueue<int[]> queue = new PriorityQueue<int[]>(new Comparator<int[]>() {
            public int compare(int[] m, int[] n) {
                return m[1] - n[1];
            }
        });

        for (Map.Entry<Integer, Integer> entry: occurrences.entrySet() ) {
            int num = entry.getKey(), count = entry.getValue();
            if (queue.size() == k) {
                if (queue.peek()[1] < count) {
                    queue.poll();
                    queue.offer(new int[]{num, count});
                }                
            } else {
                queue.offer(new int[]{num, count});
            }
        }

        int[] ret = new int[k];
        for (int i=0; i<k; i++) {
            ret[i] = queue.poll()[0];
        }
        return ret;
    }
}
// @lc code=end

