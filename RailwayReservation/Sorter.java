import java.util.*;

/** Hand-written merge sort (stable, O(n log n)) used for sorting trains. */
public class Sorter {

    public static <T> List<T> mergeSort(List<T> list, Comparator<T> cmp) {
        if (list.size() <= 1) return new ArrayList<>(list);
        int mid = list.size() / 2;
        List<T> left = mergeSort(list.subList(0, mid), cmp);
        List<T> right = mergeSort(list.subList(mid, list.size()), cmp);
        List<T> merged = new ArrayList<>(list.size());
        int i = 0, j = 0;
        while (i < left.size() && j < right.size()) {
            if (cmp.compare(left.get(i), right.get(j)) <= 0) merged.add(left.get(i++));
            else merged.add(right.get(j++));
        }
        while (i < left.size())  merged.add(left.get(i++));
        while (j < right.size()) merged.add(right.get(j++));
        return merged;
    }
}
