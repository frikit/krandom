/*
 * Copyright (c) 2026 krandom contributors
 *
 * Licensed under the MIT License. See LICENSE in the project root for license information.
 */
/**
 * Core kRandom generators, object fixtures, providers, and schema support.
 */
module io.github.frikit.krandom {
    requires jakarta.validation;
    requires java.sql;
    requires org.objenesis;
    requires org.slf4j;
    requires static transitive org.jspecify;

    uses io.github.frikit.krandom.generator.object.ObjectConstructionAdapter;

    exports io.github.frikit.krandom.generator;
    exports io.github.frikit.krandom.generator.algorithms;
    exports io.github.frikit.krandom.generator.base;
    exports io.github.frikit.krandom.generator.color;
    exports io.github.frikit.krandom.generator.commerce;
    exports io.github.frikit.krandom.generator.database;
    exports io.github.frikit.krandom.generator.datapack;
    exports io.github.frikit.krandom.generator.datetime;
    exports io.github.frikit.krandom.generator.failure;
    exports io.github.frikit.krandom.generator.file;
    exports io.github.frikit.krandom.generator.finance;
    exports io.github.frikit.krandom.generator.games.coin;
    exports io.github.frikit.krandom.generator.games.dice;
    exports io.github.frikit.krandom.generator.identifier;
    exports io.github.frikit.krandom.generator.locale;
    exports io.github.frikit.krandom.generator.location;
    exports io.github.frikit.krandom.generator.measurement;
    exports io.github.frikit.krandom.generator.namespace;
    exports io.github.frikit.krandom.generator.network;
    exports io.github.frikit.krandom.generator.object;
    exports io.github.frikit.krandom.generator.object.exception;
    exports io.github.frikit.krandom.generator.provider;
    exports io.github.frikit.krandom.generator.schema;
    exports io.github.frikit.krandom.generator.selection;
    exports io.github.frikit.krandom.generator.system;
    exports io.github.frikit.krandom.generator.tech;
    exports io.github.frikit.krandom.generator.text;
    exports io.github.frikit.krandom.generator.user;
    exports io.github.frikit.krandom.generator.user.nationalid;
    exports io.github.frikit.krandom.generator.vehicle;
    exports io.github.frikit.krandom.generator.weather;
}
