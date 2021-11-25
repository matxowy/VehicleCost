package com.matxowy.vehiclecost.util

// rozszerzenie pola w celu przemienienia deklaracji w wyrażenie
val <T> T.exhaustive: T // możemy użyć na każdym typie
    get() = this // zwraca ten sam obiekt

