/*
 * Copyright 2013 Red Hat, Inc. and/or its affiliates.
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

package org.optaplanner.core.api.score.buildin.bendable;

import java.util.Arrays;

import org.kie.api.runtime.rule.RuleContext;
import org.optaplanner.core.api.score.Score;
import org.optaplanner.core.api.score.holder.AbstractScoreHolder;

/**
 * @see BendableScore
 */
public class BendableScoreHolder extends AbstractScoreHolder {

    private int[] hardScores;
    private int[] softScores;

    public BendableScoreHolder(boolean constraintMatchEnabled, int hardLevelsSize, int softLevelsSize) {
        super(constraintMatchEnabled);
        hardScores = new int[hardLevelsSize];
        softScores = new int[softLevelsSize];
    }

    public int getHardLevelsSize() {
        return hardScores.length;
    }

    public int getHardScore(int hardLevel) {
        return hardScores[hardLevel];
    }

    public int getSoftLevelsSize() {
        return softScores.length;
    }

    public int getSoftScore(int softLevel) {
        return softScores[softLevel];
    }

    // ************************************************************************
    // Worker methods
    // ************************************************************************

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param hardLevel {@code 0 <= hardLevel <} {@link #getHardLevelsSize()}.
     * The {@code scoreLevel} is {@code hardLevel} for hard levels and {@code softLevel + hardLevelSize} for soft levels.
     * @param weight higher is better, negative for a penalty, positive for a reward
     */
    public void addHardConstraintMatch(RuleContext kcontext, final int hardLevel, final int weight) {
        hardScores[hardLevel] += weight;
        registerIntConstraintMatch(kcontext, hardLevel, weight, new IntConstraintUndoListener() {
            @Override
            public void undo() {
                hardScores[hardLevel] -= weight;
            }
        });
    }

    /**
     * @param kcontext never null, the magic variable in DRL
     * @param softLevel {@code 0 <= softLevel <} {@link #getSoftLevelsSize()}.
     * The {@code scoreLevel} is {@code hardLevel} for hard levels and {@code softLevel + hardLevelSize} for soft levels.
     * @param weight higher is better, negative for a penalty, positive for a reward
     */
    public void addSoftConstraintMatch(RuleContext kcontext, final int softLevel, final int weight) {
        softScores[softLevel] += weight;
        registerIntConstraintMatch(kcontext, getHardLevelsSize() + softLevel, weight, new IntConstraintUndoListener() {
            @Override
            public void undo() {
                softScores[softLevel] -= weight;
            }
        });
    }

    @Override
    public Score extractScore(int initScore) {
        return new BendableScore(initScore,
                Arrays.copyOf(hardScores, hardScores.length),
                Arrays.copyOf(softScores, softScores.length));
    }

}
