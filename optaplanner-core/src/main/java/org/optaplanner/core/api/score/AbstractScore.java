/*
 * Copyright 2010 Red Hat, Inc. and/or its affiliates.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.optaplanner.core.api.score;

import java.io.Serializable;
import java.math.BigDecimal;

import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;

/**
 * Abstract superclass for {@link Score}.
 * <p>
 * Subclasses must be immutable.
 * @param <S> the actual score type
 * @see Score
 * @see HardSoftScore
 */
public abstract class AbstractScore<S extends Score> implements Score<S>, Serializable {

    protected static final String INIT_LABEL = "init";

    protected static String[] parseScoreTokens(Class<? extends Score> scoreClass,
            String scoreString, String... levelSuffixes) {
        String[] scoreTokens = new String[levelSuffixes.length + 1];
        String[] suffixedScoreTokens = scoreString.split("/");
        int startIndex;
        if (suffixedScoreTokens.length == levelSuffixes.length + 1) {
            String suffixedScoreToken = suffixedScoreTokens[0];
            if (!suffixedScoreToken.endsWith(INIT_LABEL)) {
                throw new IllegalArgumentException("The scoreString (" + scoreString
                        + ") for the scoreClass (" + scoreClass.getSimpleName()
                        + ") doesn't follow the correct pattern (" + buildScorePattern(false, levelSuffixes) + "):"
                        + " the suffixedScoreToken (" + suffixedScoreToken
                        + ") does not end with levelSuffix (" + INIT_LABEL + ").");
            }
            scoreTokens[0] = suffixedScoreToken.substring(0, suffixedScoreToken.length() - INIT_LABEL.length());
            startIndex = 1;
        } else if (suffixedScoreTokens.length == levelSuffixes.length) {
            scoreTokens[0] = "0";
            startIndex = 0;
        } else {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + scoreClass.getSimpleName()
                    + ") doesn't follow the correct pattern (" + buildScorePattern(false, levelSuffixes) + "):"
                    + " the suffixedScoreTokens length (" + suffixedScoreTokens.length
                    + ") differs from the levelSuffixes length ("
                    + levelSuffixes.length + " or " + (levelSuffixes.length + 1) + ").");
        }
        for (int i = 0; i < levelSuffixes.length; i++) {
            String suffixedScoreToken = suffixedScoreTokens[startIndex + i];
            String levelSuffix = levelSuffixes[i];
            if (!suffixedScoreToken.endsWith(levelSuffix)) {
                throw new IllegalArgumentException("The scoreString (" + scoreString
                        + ") for the scoreClass (" + scoreClass.getSimpleName()
                        + ") doesn't follow the correct pattern (" + buildScorePattern(false, levelSuffixes) + "):"
                        + " the suffixedScoreToken (" + suffixedScoreToken
                        + ") does not end with levelSuffix (" + levelSuffix + ").");
            }
            scoreTokens[1 + i] = suffixedScoreToken.substring(0, suffixedScoreToken.length() - levelSuffix.length());
        }
        return scoreTokens;
    }

    protected static int parseInitScore(Class<? extends Score> scoreClass,
            String scoreString, String initScoreString) {
        try {
            return Integer.parseInt(initScoreString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + scoreClass.getSimpleName() + ") has a initScoreString ("
                    + initScoreString + ") which is not a valid integer.", e);
        }
    }

    protected static int parseLevelAsInt(Class<? extends Score> scoreClass,
            String scoreString, String levelString) {
        try {
            return Integer.parseInt(levelString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + scoreClass.getSimpleName() + ") has a levelString (" + levelString
                    + ") which is not a valid integer.", e);
        }
    }

    protected static long parseLevelAsLong(Class<? extends Score> scoreClass,
            String scoreString, String levelString) {
        try {
            return Long.parseLong(levelString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + scoreClass.getSimpleName() + ") has a levelString (" + levelString
                    + ") which is not a valid long.", e);
        }
    }

    protected static double parseLevelAsDouble(Class<? extends Score> scoreClass,
            String scoreString, String levelString) {
        try {
            return Double.parseDouble(levelString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + scoreClass.getSimpleName() + ") has a levelString (" + levelString
                    + ") which is not a valid double.", e);
        }
    }

    protected static BigDecimal parseLevelAsBigDecimal(Class<? extends Score> scoreClass,
            String scoreString, String levelString) {
        try {
            return new BigDecimal(levelString);
        } catch (NumberFormatException e) {
            throw new IllegalArgumentException("The scoreString (" + scoreString
                    + ") for the scoreClass (" + scoreClass.getSimpleName() + ") has a levelString (" + levelString
                    + ") which is not a valid BigDecimal.", e);
        }
    }

    protected static String buildScorePattern(boolean bendable, String... levelSuffixes) {
        StringBuilder scorePattern = new StringBuilder(levelSuffixes.length * 10);
        boolean first = true;
        for (String levelSuffix : levelSuffixes) {
            if (first) {
                first = false;
            } else {
                scorePattern.append("/");
            }
            if (bendable) {
                scorePattern.append("[999/.../999]");
            } else {
                scorePattern.append("999");
            }
            scorePattern.append(levelSuffix);
        }
        return scorePattern.toString();
    }

    // ************************************************************************
    // Fields
    // ************************************************************************

    protected final int initScore;

    /**
     * @param initScore see {@link Score#getInitScore()}
     */
    protected AbstractScore(int initScore) {
        this.initScore = initScore;
        // The initScore can be positive during statistical calculations.
    }

    @Override
    public int getInitScore() {
        return initScore;
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    @Override
    public boolean isSolutionInitialized() {
        return initScore >= 0;
    }

    protected void assertNoInitScore() {
        if (initScore != 0) {
            throw new IllegalStateException("The score (" + this + ")'s initScore (" + initScore
                    + ") should be 0.\n"
                    + "Maybe the score calculator is calculating the initScore too,"
                    + " although it's the score director's responsibility.");
        }
    }

    protected String getInitPrefix() {
        if (initScore == 0) {
            return "";
        }
        return initScore + INIT_LABEL + "/";
    }

}
