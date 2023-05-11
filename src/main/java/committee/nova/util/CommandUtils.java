package committee.nova.util;

import com.google.common.base.Functions;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import net.minecraft.util.ResourceLocation;

import java.util.*;

import static net.minecraft.command.CommandBase.doesStringStartWith;
import static net.minecraft.command.CommandBase.joinNiceString;

@SuppressWarnings("unchecked")
public class CommandUtils {
    public static String joinNiceStringFromCollection(Collection<String> strings) {
        return joinNiceString(strings.toArray(new String[0]));
    }

    public static <E> void sort(List<E> list, Comparator<? super E> c) {
        final E[] a = (E[]) list.toArray();
        Arrays.sort(a, c);
        final ListIterator<E> i = list.listIterator();
        for (E e : a) {
            i.next();
            i.set(e);
        }
    }

    public static List<String> getListOfStringsMatchingLastWord(String[] inputArgs, Collection<?> possibleCompletions) {
        final String s = inputArgs[inputArgs.length - 1];
        final List<String> list = Lists.<String>newArrayList();
        if (!possibleCompletions.isEmpty()) {
            for (String s1 : Iterables.transform(possibleCompletions, Functions.toStringFunction()))
                if (doesStringStartWith(s, s1)) list.add(s1);
            if (list.isEmpty()) for (Object object : possibleCompletions)
                if (object instanceof ResourceLocation && doesStringStartWith(s, ((ResourceLocation) object).getResourcePath()))
                    list.add(String.valueOf(object));
        }
        return list;
    }
}
