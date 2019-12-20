package org.github.krandom.user.enum

enum class AgeGroup(val groupName: String, val range: ClosedRange<Int>) {
    CHILD("child", 0..14),
    TEEN("teen", 14..18),
    ADULT("adult", 18..60),
    SENIOR("senior", 60..99);
}
