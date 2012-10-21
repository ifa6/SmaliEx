/*
 * Copyright 2012, Google Inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are
 * met:
 *
 *     * Redistributions of source code must retain the above copyright
 * notice, this list of conditions and the following disclaimer.
 *     * Redistributions in binary form must reproduce the above
 * copyright notice, this list of conditions and the following disclaimer
 * in the documentation and/or other materials provided with the
 * distribution.
 *     * Neither the name of Google Inc. nor the names of its
 * contributors may be used to endorse or promote products derived from
 * this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
 * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR
 * A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
 * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package org.jf.dexlib2.dexbacked.value;

import org.jf.dexlib2.dexbacked.DexFileBuffer;
import org.jf.dexlib2.dexbacked.DexFileReader;
import org.jf.dexlib2.ValueType;
import org.jf.dexlib2.dexbacked.DexBackedAnnotationElement;
import org.jf.dexlib2.dexbacked.util.VariableSizeList;
import org.jf.dexlib2.iface.AnnotationElement;
import org.jf.dexlib2.iface.value.AnnotationEncodedValue;

import javax.annotation.Nonnull;
import java.util.List;

public class DexBackedAnnotationEncodedValue implements AnnotationEncodedValue {
    @Nonnull public final DexFileBuffer dexFile;
    public final String type;
    private final int elementsOffset;

    public DexBackedAnnotationEncodedValue(@Nonnull DexFileReader dexFileReader) {
        this.dexFile = dexFileReader.getDexFile();
        this.type = dexFileReader.getString(dexFileReader.readSmallUleb128());
        this.elementsOffset = dexFileReader.getOffset();
        skipElements(dexFileReader);
    }

    public static void skipFrom(DexFileReader reader) {
        reader.skipUleb128();
        skipElements(reader);
    }

    private static void skipElements(DexFileReader reader) {
        int elementCount = reader.readSmallUleb128();
        for (int i=0; i<elementCount; i++) {
            reader.skipUleb128();
            DexBackedEncodedValue.skipFrom(reader);
        }
    }

    @Override public int getValueType() { return ValueType.ANNOTATION; }
    @Nonnull @Override public String getType() { return type; }

    @Nonnull
    @Override
    public List<? extends AnnotationElement> getElements() {
        DexFileReader reader = dexFile.readerAt(elementsOffset);
        final int size = reader.readSmallUleb128();

        return new VariableSizeList<AnnotationElement>(dexFile, reader.getOffset()) {
            @Nonnull
            @Override
            protected AnnotationElement readItem(DexFileReader dexFileReader, int index) {
                return new DexBackedAnnotationElement(dexFileReader);
            }

            @Override public int size() { return size;}
        };
    }
}