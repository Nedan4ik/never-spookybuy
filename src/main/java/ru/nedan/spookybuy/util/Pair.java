package ru.nedan.spookybuy.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class Pair<A, B> {
    private A left;
    private B right;
}
