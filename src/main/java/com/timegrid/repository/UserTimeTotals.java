package com.timegrid.repository;

import java.util.UUID;

public interface UserTimeTotals {

    UUID getUserId();

    String getFullName();

    String getTeamName();

    long getEntries();

    long getMinutes();

}