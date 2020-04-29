/**
 * Copyright 2014-2020 the original author or authors.
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package node.mgr.test.alert.task;

import org.junit.Test;

import javax.validation.constraints.AssertTrue;
import javax.xml.crypto.Data;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoField;
import java.util.Date;

public class SevenDaysTest {

    @Test
    public void test7day() {
        Date date = new Date();
        System.out.println("date" + date.getTime());
        System.out.println(checkWithin7days(date));
    }

    private boolean checkWithin7days(Date certNotAfter) {
        // unit: ms
        long sevenDays = 1000 * 60 * 60 * 24 * 7;
//        long now = Instant.now().getLong(ChronoField.MILLI_OF_SECOND);
        long now = Instant.now().toEpochMilli();
        long interval = certNotAfter.getTime() - now;
        System.out.println("sevenDays" + sevenDays);
        System.out.println("now" + now);
        System.out.println("interval" + interval);
        if(interval < sevenDays) {
            // within 7days or already not valid (<0)
            return true;
        } else {
            // beyond 7days
            return false;
        }
    }
}
