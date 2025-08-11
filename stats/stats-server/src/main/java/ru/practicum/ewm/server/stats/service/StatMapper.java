package ru.practicum.ewm.server.stats.service;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.ewm.dto.stats.EndpointHit;
import ru.practicum.ewm.server.stats.repository.StatRecord;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class StatMapper {
    public static StatRecord toStatRecord(EndpointHit endpointHit) {
        StatRecord statRecord = new StatRecord();
        statRecord.setApp(endpointHit.getApp());
        statRecord.setUri(endpointHit.getUri());
        statRecord.setIp(endpointHit.getIp());
        statRecord.setTimestamp(endpointHit.getTimestamp());
        return statRecord;
    }
}
