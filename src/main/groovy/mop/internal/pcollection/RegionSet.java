/*
 * Copyright 2003-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package groovy.mop.internal.pcollection;

import java.lang.invoke.MethodHandle;
import java.util.Iterator;

public class RegionSet<T> implements PSet<T> {
    private final T[] elements;
    private final int offset, end;

    public RegionSet(T[] elements, int offset, int end) {
        this.elements = elements;
        this.offset = offset;
        this.end = end;
    }

    @Override
    public Iterator<T> iterator() {
        return new Iterator<T>() {
            private int index = offset;
            @Override
            public boolean hasNext() {
                return index<=end;
            }
            @Override
            public T next() {
                index++;
                return (T) elements[index];
            }
            @Override
            public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    @Override
    public boolean isEmpty() {
        return false;
    }

    @Override
    public PSet<T> append(PSet<T> other) {
        if (other.isEmpty()) return this;
        MultiSet ms = new MultiSet(other,null);
        return new MultiSet(this,ms);
    }

    @Override
    public PSet<T> minus(PSet<T> other, MethodHandle compare) {
        if (other.isEmpty()) return this;
        PSet<T> res = FlatSet.minus_0(elements, other, compare, offset, end);
        if (res==null) return this;
        return res;
    }

    @Override
    public PSet<T> plus(PSet<T> other, MethodHandle compare) {
        PSet a = minus(other, compare);
        MultiSet ms = new MultiSet(other,null);
        return new MultiSet(a, ms);
    }

    @Override
    public PSet<T> minus(T element) {
        int index = FlatSet.find_0(elements, element, offset, end);
        if (index==-1) return this;
        if (end-offset==2) {
            T remainder;
            if (index==offset) {
                remainder = elements[offset+1];
            } else {
                remainder = elements[offset];
            }
            return new SingleElementSet(remainder);
        } else {
            if (index==end-1) {
                return new RegionSet(elements, offset, end-1);
            } else if (index==offset) {
                return new RegionSet(elements, offset+1, end);
            }
            PSet after = new RegionSet(elements, index+1, end);
            MultiSet ms = new MultiSet(after,null);
            PSet before = new RegionSet(elements, offset, index-1);
            return new MultiSet(before, ms);
        }
    }

}