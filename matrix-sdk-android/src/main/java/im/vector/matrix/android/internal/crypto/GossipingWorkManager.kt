/*
 * Copyright (c) 2020 New Vector Ltd
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

package im.vector.matrix.android.internal.crypto

import androidx.work.BackoffPolicy
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.ListenableWorker
import androidx.work.OneTimeWorkRequest
import im.vector.matrix.android.api.util.Cancelable
import im.vector.matrix.android.internal.di.WorkManagerProvider
import im.vector.matrix.android.internal.session.SessionScope
import im.vector.matrix.android.internal.util.CancelableWork
import im.vector.matrix.android.internal.worker.startChain
import java.util.concurrent.TimeUnit
import javax.inject.Inject

@SessionScope
internal class GossipingWorkManager @Inject constructor(
        private val workManagerProvider: WorkManagerProvider
) {

    inline fun <reified W : ListenableWorker> createWork(data: Data, startChain: Boolean): OneTimeWorkRequest {
        return workManagerProvider.matrixOneTimeWorkRequestBuilder<W>()
                .setConstraints(WorkManagerProvider.workConstraints)
                .startChain(startChain)
                .setInputData(data)
                .setBackoffCriteria(BackoffPolicy.LINEAR, 10_000L, TimeUnit.MILLISECONDS)
                .build()
    }

    // Prevent sending queue to stay broken after app restart
    // The unique queue id will stay the same as long as this object is instanciated
    val queueSuffixApp = System.currentTimeMillis()

    fun postWork(workRequest: OneTimeWorkRequest, policy: ExistingWorkPolicy = ExistingWorkPolicy.APPEND): Cancelable {
        workManagerProvider.workManager
                .beginUniqueWork(this::class.java.name + "_$queueSuffixApp", policy, workRequest)
                .enqueue()

        return CancelableWork(workManagerProvider.workManager, workRequest.id)
    }
}
