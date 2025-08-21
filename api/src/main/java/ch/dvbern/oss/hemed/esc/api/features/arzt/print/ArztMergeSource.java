/*
 * Copyright (C) 2025 DV Bern AG, Switzerland
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as
 * published by the Free Software Foundation, either version 3 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package ch.dvbern.oss.hemed.esc.api.features.arzt.print;

import java.util.ArrayList;
import java.util.List;

import ch.dvbern.lib.doctemplate.common.BeanMergeSource;
import ch.dvbern.lib.doctemplate.common.DocTemplateException;
import ch.dvbern.lib.doctemplate.common.MergeContext;
import ch.dvbern.lib.doctemplate.common.MergeSource;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class ArztMergeSource implements MergeSource {
    private final String plz;
    private final String ort;
    private final List<ArztMergerDTO > aerzte;

    @Override
    public Object getData(MergeContext context, String key) throws DocTemplateException {
        if(key.startsWith("plzgroup.plz")){
            return plz;
        }
        if(key.startsWith("plzgroup.ort")){
            return ort;
        }
        return '[' + key + ']';
    }

    @Override
    public Boolean ifStatement(MergeContext var1, String var2) throws DocTemplateException {
        return false;
    }

    @Override
    public List<MergeSource> whileStatement(MergeContext context, String key) throws DocTemplateException {
        if (key.startsWith("plzgroup.aerzte")){
            List<MergeSource> mergeSources = new ArrayList<>();

            for (ArztMergerDTO arzt : aerzte) {
                mergeSources.add( new BeanMergeSource(arzt, "arzt."));
            }

            return  mergeSources;
        }
        return new ArrayList<>();

    }
}
