# LRU 缓存

## title
哈希表 + 双向链表

## content
## 解题思路

### 算法：哈希表 + 双向链表

使用 `LinkedHashMap`（或手写双向链表 + HashMap）实现 O(1) 的 get/put：
- `get(key)`：存在则返回值并移到末尾（最近使用），否则返回 -1
- `put(key, value)`：已存在则更新并移到末尾；不存在则插入，超出容量则删除头部（最久未使用）

输入格式：第一行为函数名数组，第二行为对应参数数组。输出为每次操作的返回值。

### 复杂度
- 时间：O(1) per operation
- 空间：O(capacity)

## code
```java
import java.util.*;
import java.io.*;

public class Solution {

    public static void main(String[] args) throws Exception {
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
        String line1 = br.readLine().trim();
        String line2 = br.readLine().trim();

        // 解析函数名 ["LRUCache","put","get",...]
        String[] ops = parseStringArray(line1);
        // 解析参数 [[2],[1,1],[1],...]
        List<int[]> params = parseIntArrayArray(line2);

        List<String> results = new ArrayList<>();
        Map<Integer, Integer> cache = null;
        int capacity = 0;

        // 使用 LinkedHashMap 实现 LRU
        for (int i = 0; i < ops.length; i++) {
            if (ops[i].equals("LRUCache")) {
                capacity = params.get(i)[0];
                int cap = capacity;
                cache = new LinkedHashMap<>(16, 0.75f, true) {
                    @Override
                    protected boolean removeEldestEntry(Map.Entry eldest) {
                        return size() > cap;
                    }
                };
                results.add("null");
            } else if (ops[i].equals("put")) {
                cache.put(params.get(i)[0], params.get(i)[1]);
                results.add("null");
            } else if (ops[i].equals("get")) {
                int val = cache.getOrDefault(params.get(i)[0], -1);
                results.add(String.valueOf(val));
            }
        }

        System.out.println("[" + String.join(",", results) + "]");
    }

    static String[] parseStringArray(String s) {
        s = s.substring(1, s.length() - 1);
        String[] parts = s.split(",");
        String[] result = new String[parts.length];
        for (int i = 0; i < parts.length; i++) {
            result[i] = parts[i].trim().replace("\"", "");
        }
        return result;
    }

    static List<int[]> parseIntArrayArray(String s) {
        List<int[]> result = new ArrayList<>();
        s = s.substring(1, s.length() - 1);
        int depth = 0;
        StringBuilder buf = new StringBuilder();
        for (char c : s.toCharArray()) {
            if (c == '[') {
                depth++;
                if (depth == 2) buf = new StringBuilder();
            } else if (c == ']') {
                depth--;
                if (depth == 1) {
                    String[] parts = buf.toString().split(",");
                    int[] arr = new int[parts.length];
                    for (int i = 0; i < parts.length; i++) arr[i] = Integer.parseInt(parts[i].trim());
                    result.add(arr);
                }
            } else if (depth == 2 && c != ',') {
                buf.append(c);
            }
        }
        return result;
    }
}
```
