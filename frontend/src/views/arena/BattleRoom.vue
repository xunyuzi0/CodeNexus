<template>
  <div
    v-if="isVerifying"
    class="h-screen w-full bg-black flex items-center justify-center font-sans"
  >
    <div class="flex flex-col items-center gap-6">
      <div class="relative w-16 h-16">
        <div class="absolute inset-0 rounded-full border-4 border-zinc-800"></div>
        <div class="absolute inset-0 rounded-full border-4 border-t-[#FF4C00] animate-spin"></div>
        <Loader2 class="absolute inset-0 m-auto w-6 h-6 text-[#FF4C00] animate-pulse" />
      </div>
      <div class="text-center space-y-2">
        <h3 class="text-white font-bold tracking-wider text-lg">沙箱环境初始化</h3>
        <p class="text-zinc-500 font-mono text-xs tracking-[0.2em] uppercase animate-pulse">
          Establishing Secure Link...
        </p>
      </div>
    </div>
  </div>

  <div v-else class="h-screen w-full bg-zinc-950 text-white flex flex-col overflow-hidden relative">
    <div class="absolute inset-0 bg-grid-white/[0.02] pointer-events-none"></div>
    <div
      class="absolute top-0 right-0 w-[500px] h-[500px] bg-[#FF4C00]/10 blur-[120px] rounded-full pointer-events-none transition-opacity duration-1000"
      :class="battleStatus === 'FIGHTING' ? 'opacity-100' : 'opacity-0'"
    ></div>

    <header
      class="h-14 shrink-0 flex items-center justify-between px-4 bg-zinc-950/80 backdrop-blur-md border-b border-white/5 z-50 relative select-none"
    >
      <div class="flex items-center gap-4">
        <ArenaExitButton @click="handleExitClick" />
      </div>

      <div
        class="absolute left-1/2 top-1/2 -translate-x-1/2 -translate-y-1/2 flex items-center gap-3 px-5 py-1.5 rounded-full backdrop-blur-xl border shadow-lg transition-all duration-500"
        :class="
          timer < 60 && battleStatus === 'FIGHTING'
            ? 'bg-red-500/10 border-red-500/50 shadow-red-500/20 animate-pulse'
            : 'bg-zinc-900/80 border-white/10'
        "
      >
        <Timer class="w-4 h-4" :class="timer < 60 ? 'text-red-500' : 'text-[#FF4C00]'" />
        <span
          class="font-mono text-base font-bold tracking-widest tabular-nums"
          :class="timer < 60 ? 'text-red-500' : 'text-white'"
        >
          {{ formattedTime }}
        </span>
        <div class="w-[1px] h-3 bg-white/20"></div>
        <span class="text-xs font-bold tracking-wider" :class="statusColor">{{ statusText }}</span>
      </div>

      <div class="flex items-center gap-2">
        <div class="relative group cursor-pointer mr-2">
          <div
            class="w-8 h-8 rounded-full bg-zinc-800 border border-white/10 overflow-hidden ring-2 ring-transparent group-hover:ring-[#FF4C00]/20 transition-all"
          >
            <img
              src="https://api.dicebear.com/7.x/avataaars/svg?seed=Felix"
              alt="User"
              class="w-full h-full object-cover opacity-90 group-hover:opacity-100"
            />
          </div>
          <div
            class="absolute bottom-0 right-0 w-2.5 h-2.5 bg-emerald-500 border-2 border-zinc-950 rounded-full"
          ></div>
        </div>
      </div>
    </header>

    <div class="flex-1 min-h-0 relative z-10 w-full max-w-[1920px] mx-auto">
      <splitpanes class="zeekr-theme" horizontal>
        <pane size="100">
          <splitpanes vertical @resize="handleResize" class="h-full">
            <pane
              v-show="paneSize.left > 0"
              :size="paneSize.left"
              :min-size="maximizedPane !== 'none' ? 0 : 20"
              class="flex flex-col min-w-0 transition-[width] duration-300 ease-in-out bg-zinc-900/40 backdrop-blur-sm border-r border-white/5"
            >
              <ProblemDescription
                :problem="problem"
                :is-maximized="maximizedPane === 'left'"
                :hide-tabs="true"
                @toggle-maximize="toggleMaximizeLeft"
              />
            </pane>

            <pane
              v-show="paneSize.middle > 0"
              :size="paneSize.middle"
              :min-size="maximizedPane !== 'none' ? 0 : 30"
              class="flex flex-col relative min-w-0 transition-[width] duration-300 ease-in-out bg-[#1e1e1e]"
            >
              <div class="flex-1 min-h-0 relative z-0">
                <splitpanes
                  class="default-theme zeekr-theme"
                  horizontal
                  @resize="handleConsoleResize"
                >
                  <pane
                    :size="consoleSizes.editor"
                    :min-size="isConsoleOpen ? 10 : 100"
                    class="flex flex-col min-h-0 relative"
                  >
                    <div
                      v-if="isViewingOpponentCode"
                      class="absolute top-0 left-0 right-0 z-50 bg-amber-500/90 text-black text-xs font-bold px-3 py-1.5 flex items-center justify-center gap-2 shadow-lg backdrop-blur-sm"
                    >
                      <Eye class="w-3.5 h-3.5" />
                      <span>正在查看对手代码 (只读模式)</span>
                    </div>

                    <div
                      class="flex-1 relative overflow-hidden"
                      :class="{ 'ring-2 ring-amber-500/50': isViewingOpponentCode }"
                    >
                      <CodeEditor
                        v-model="editorCode"
                        language="java"
                        class="h-full w-full"
                        :read-only="isViewingOpponentCode || battleStatus !== 'FIGHTING'"
                        :is-maximized="maximizedPane === 'middle'"
                        @toggle-maximize="toggleMaximizeMiddle"
                      />
                    </div>
                  </pane>

                  <pane
                    v-if="isConsoleOpen"
                    :size="consoleSizes.console"
                    class="bg-zinc-900 flex flex-col min-h-0 border-t border-white/5 relative z-10"
                  >
                    <div
                      class="h-9 shrink-0 flex items-center justify-between px-2 bg-zinc-950 border-b border-white/5 select-none"
                    >
                      <div class="flex items-center h-full gap-1">
                        <button
                          v-for="tab in ['testcase', 'result'] as const"
                          :key="tab"
                          @click="activeConsoleTab = tab"
                          class="h-full px-4 text-xs font-medium relative transition-colors flex items-center gap-2 group"
                          :class="
                            activeConsoleTab === tab
                              ? 'text-white'
                              : 'text-zinc-500 hover:text-zinc-300'
                          "
                        >
                          <component
                            :is="tab === 'testcase' ? FlaskConical : Terminal"
                            class="w-3.5 h-3.5"
                          />
                          {{ tab === 'testcase' ? '测试用例' : '执行结果' }}
                          <div
                            v-if="activeConsoleTab === tab"
                            class="absolute bottom-0 left-0 right-0 h-[2px] bg-[#FF4C00] shadow-[0_-2px_6px_rgba(255,76,0,0.4)]"
                          ></div>
                        </button>
                      </div>
                      <button
                        @click="toggleConsole"
                        class="mr-2 p-1.5 rounded-lg text-zinc-500 hover:text-white hover:bg-white/5 transition-colors"
                        title="收起控制台"
                      >
                        <ChevronDown class="w-4 h-4" />
                      </button>
                    </div>

                    <div class="flex-1 overflow-y-auto custom-scrollbar p-4 bg-zinc-900/50">
                      <div
                        v-show="activeConsoleTab === 'testcase'"
                        class="space-y-5 animate-in fade-in slide-in-from-bottom-2 duration-300 h-full flex flex-col"
                      >
                        <div class="flex items-center gap-2 overflow-x-auto no-scrollbar shrink-0">
                          <div
                            v-for="(c, index) in testCases"
                            :key="index"
                            class="flex items-center"
                          >
                            <button
                              @click="activeCaseIndex = index"
                              class="px-3 py-1.5 rounded-lg text-xs font-mono transition-all border flex items-center gap-1.5 group/tab"
                              :class="
                                activeCaseIndex === index
                                  ? 'bg-zinc-800 text-white border-white/10 shadow-sm'
                                  : 'border-transparent text-zinc-500 hover:bg-zinc-800/50 hover:text-zinc-300'
                              "
                            >
                              Case {{ index + 1 }}
                              <X
                                v-if="activeCaseIndex === index && testCases.length > 1"
                                @click.stop="handleRemoveTestCase(index)"
                                class="w-3 h-3 text-zinc-500 hover:text-red-500 cursor-pointer transition-colors"
                              />
                            </button>
                          </div>
                          <button
                            @click="handleAddTestCase"
                            class="p-1.5 rounded-lg text-zinc-500 hover:text-white hover:bg-white/10 transition-colors border border-dashed border-zinc-700"
                          >
                            <Plus class="w-3.5 h-3.5" />
                          </button>
                        </div>
                        <div class="space-y-4 flex-1 overflow-y-auto pr-2">
                          <template v-if="testCases[activeCaseIndex]">
                            <div
                              v-for="(_, key) in testCases[activeCaseIndex].inputs"
                              :key="key"
                              class="space-y-2"
                            >
                              <label
                                class="block text-xs font-medium text-zinc-500 uppercase tracking-wider font-mono pl-1"
                                >{{ key }} =</label
                              >
                              <input
                                type="text"
                                v-model="testCases[activeCaseIndex].inputs[key]"
                                class="w-full bg-black/30 border border-white/10 rounded-lg px-4 py-3 font-mono text-sm text-zinc-300 focus:border-[#FF4C00] focus:ring-1 focus:ring-[#FF4C00]/20 outline-none transition-all"
                                spellcheck="false"
                              />
                            </div>
                          </template>
                        </div>
                      </div>

                      <div v-show="activeConsoleTab === 'result'" class="h-full">
                        <div
                          v-if="isRunning && !showJudgePanel"
                          class="h-full flex flex-col items-center justify-center space-y-4"
                        >
                          <Loader2 class="w-8 h-8 text-[#FF4C00] animate-spin" />
                          <p class="text-xs text-zinc-500 font-mono animate-pulse">
                            Running code in Sandbox...
                          </p>
                        </div>
                        <div
                          v-else-if="runResults.length === 0"
                          class="h-full flex flex-col items-center justify-center text-zinc-600 space-y-3"
                        >
                          <div
                            class="w-12 h-12 rounded-2xl bg-zinc-800/50 flex items-center justify-center"
                          >
                            <Terminal class="w-6 h-6 opacity-40" />
                          </div>
                          <p class="text-xs font-medium">请点击“运行测试”以执行代码</p>
                        </div>
                        <div
                          v-else
                          class="animate-in fade-in slide-in-from-bottom-2 flex flex-col h-full gap-4"
                        >
                          <div
                            class="flex items-center gap-2 overflow-x-auto no-scrollbar shrink-0"
                          >
                            <button
                              v-for="(res, idx) in runResults"
                              :key="idx"
                              @click="activeResultIndex = idx"
                              class="px-3 py-1.5 rounded-lg text-xs font-mono transition-all border flex items-center gap-2"
                              :class="[
                                activeResultIndex === idx
                                  ? res.status === 'AC'
                                    ? 'bg-emerald-500/10 text-emerald-500 border-emerald-500/20 shadow-sm'
                                    : 'bg-rose-500/10 text-rose-500 border-rose-500/20 shadow-sm'
                                  : 'border-transparent text-zinc-500 hover:bg-zinc-800/50 hover:text-zinc-300',
                              ]"
                            >
                              <span
                                class="w-1.5 h-1.5 rounded-full"
                                :class="
                                  res.status === 'AC'
                                    ? 'bg-emerald-500 shadow-[0_0_8px_rgba(16,185,129,0.8)]'
                                    : 'bg-rose-500 shadow-[0_0_8px_rgba(244,63,94,0.8)]'
                                "
                              ></span>
                              Case {{ idx + 1 }}
                            </button>
                          </div>
                          <div
                            v-if="runResults[activeResultIndex]"
                            class="flex-1 overflow-y-auto pr-2 pb-2"
                          >
                            <div
                              class="flex items-center gap-3 px-4 py-3 rounded-xl border mb-4"
                              :class="
                                runResults[activeResultIndex].status === 'AC'
                                  ? 'bg-emerald-500/10 border-emerald-500/20'
                                  : 'bg-rose-500/10 border-rose-500/20'
                              "
                            >
                              <span
                                class="text-sm font-bold uppercase tracking-wider"
                                :class="
                                  runResults[activeResultIndex].status === 'AC'
                                    ? 'text-emerald-500'
                                    : 'text-rose-500'
                                "
                                >{{ getStatusText(runResults[activeResultIndex].status) }}</span
                              >
                              <span
                                class="w-[1px] h-3"
                                :class="
                                  runResults[activeResultIndex].status === 'AC'
                                    ? 'bg-emerald-500/20'
                                    : 'bg-rose-500/20'
                                "
                              ></span>
                              <span
                                class="text-xs font-mono"
                                :class="
                                  runResults[activeResultIndex].status === 'AC'
                                    ? 'text-emerald-400/80'
                                    : 'text-rose-400/80'
                                "
                                >Runtime: {{ runResults[activeResultIndex].runtime }}</span
                              >
                            </div>
                            <div
                              class="bg-black/30 border border-white/10 rounded-xl p-4 font-mono text-xs space-y-4 shadow-inner"
                            >
                              <div class="space-y-1.5">
                                <span
                                  class="text-zinc-500 block uppercase tracking-wider scale-90 origin-left"
                                  >Input</span
                                >
                                <div
                                  class="text-zinc-300 bg-zinc-800/50 border border-white/5 px-3 py-2.5 rounded-lg whitespace-pre-wrap leading-relaxed"
                                >
                                  {{ runResults[activeResultIndex].input }}
                                </div>
                              </div>
                              <div class="space-y-1.5">
                                <span
                                  class="text-zinc-500 block uppercase tracking-wider scale-90 origin-left"
                                  >Actual Output</span
                                >
                                <div
                                  class="bg-zinc-800/50 border border-white/5 px-3 py-2.5 rounded-lg"
                                  :class="
                                    runResults[activeResultIndex].status === 'AC'
                                      ? 'text-emerald-500'
                                      : 'text-rose-500'
                                  "
                                >
                                  {{ runResults[activeResultIndex].output }}
                                </div>
                              </div>
                              <div class="space-y-1.5">
                                <span
                                  class="text-zinc-500 block uppercase tracking-wider scale-90 origin-left"
                                  >Expected Output</span
                                >
                                <div
                                  class="text-emerald-500 bg-zinc-800/50 border border-white/5 px-3 py-2.5 rounded-lg"
                                >
                                  {{ runResults[activeResultIndex].expected }}
                                </div>
                              </div>
                            </div>
                          </div>
                        </div>
                      </div>
                    </div>
                  </pane>
                </splitpanes>
              </div>

              <div
                class="h-14 shrink-0 bg-zinc-950/80 backdrop-blur-xl border-t border-white/5 px-6 flex items-center justify-between z-30"
              >
                <div class="flex items-center gap-2">
                  <button
                    v-if="!isConsoleOpen"
                    @click="toggleConsole"
                    class="flex items-center gap-2 px-3 py-1.5 rounded-lg text-zinc-400 hover:text-white hover:bg-white/5 transition-colors text-xs font-medium active:scale-95 duration-200 group"
                  >
                    <div class="relative">
                      <SquareTerminal
                        class="w-4 h-4 group-hover:text-[#FF4C00] transition-colors"
                      />
                      <span class="absolute -top-0.5 -right-0.5 flex h-1.5 w-1.5">
                        <span
                          class="animate-ping absolute inline-flex h-full w-full rounded-full bg-[#FF4C00] opacity-75"
                        ></span>
                        <span
                          class="relative inline-flex rounded-full h-1.5 w-1.5 bg-[#FF4C00]"
                        ></span>
                      </span>
                    </div>
                    <span>控制台</span>
                    <ChevronUp class="w-3 h-3 ml-1 opacity-50" />
                  </button>
                </div>
                <div class="flex items-center gap-3">
                  <button
                    @click="handleRunTest"
                    :disabled="
                      isRunning ||
                      isSubmitting ||
                      battleStatus !== 'FIGHTING' ||
                      isViewingOpponentCode
                    "
                    class="px-5 py-2 rounded-lg bg-zinc-800 text-zinc-200 border border-white/5 hover:bg-zinc-700 hover:text-white transition-all font-bold text-sm flex items-center gap-2 active:scale-95 disabled:opacity-50 disabled:cursor-not-allowed group"
                  >
                    <Play class="w-4 h-4 fill-zinc-400 group-hover:fill-white transition-colors" />
                    运行测试
                  </button>
                  <button
                    @click="handleSubmit"
                    :disabled="
                      isRunning ||
                      isSubmitting ||
                      battleStatus !== 'FIGHTING' ||
                      isViewingOpponentCode
                    "
                    class="px-8 py-2 rounded-lg bg-[#FF4C00] text-white font-bold text-sm flex items-center gap-2 shadow-[0_0_15px_rgba(255,76,0,0.3)] hover:shadow-[0_0_25px_rgba(255,76,0,0.5)] hover:bg-[#ff5f1f] transition-all active:scale-95 disabled:opacity-50 disabled:shadow-none disabled:cursor-not-allowed"
                  >
                    <Loader2 v-if="isSubmitting" class="w-4 h-4 animate-spin" />
                    <Send v-else class="w-4 h-4" />
                    {{ isSubmitting ? '提交中...' : '提交代码' }}
                  </button>
                </div>
              </div>
            </pane>

            <pane
              v-show="paneSize.right > 0"
              :size="paneSize.right"
              :min-size="15"
              class="flex flex-col relative min-w-0 transition-[width] duration-300 ease-in-out bg-zinc-900/40 backdrop-blur-sm border-l border-white/5"
            >
              <div
                class="h-10 shrink-0 flex items-center px-4 bg-zinc-900/80 border-b border-white/5 select-none z-10"
              >
                <span
                  class="text-xs font-bold text-zinc-400 uppercase tracking-widest flex items-center gap-2"
                >
                  <Swords class="w-4 h-4 text-zinc-500" /> 战况雷达
                </span>
              </div>

              <div class="flex-1 flex flex-col p-4 gap-4 min-h-0 overflow-hidden">
                <div
                  class="bg-zinc-950/50 border border-white/5 rounded-2xl p-4 shrink-0 space-y-5"
                >
                  <div class="space-y-4">
                    <div class="space-y-1.5">
                      <div
                        class="flex justify-between items-end text-xs uppercase font-bold tracking-wider"
                      >
                        <span class="text-[#FF4C00] flex items-center gap-1.5"
                          ><User class="w-3.5 h-3.5" /> 我方</span
                        >
                        <span class="text-zinc-400 font-mono">{{ myProgress }}% AC</span>
                      </div>
                      <div class="h-2 w-full bg-zinc-800 rounded-full overflow-hidden">
                        <div
                          class="h-full bg-[#FF4C00] shadow-[0_0_10px_#FF4C00] transition-all duration-500 ease-out"
                          :style="{ width: myProgress + '%' }"
                        ></div>
                      </div>
                    </div>
                    <div class="space-y-1.5">
                      <div
                        class="flex justify-between items-end text-xs uppercase font-bold tracking-wider"
                      >
                        <span class="text-rose-500 flex items-center gap-1.5"
                          ><ShieldAlert class="w-3.5 h-3.5" /> 对手 ({{ opponent.name }})</span
                        >
                        <span class="text-zinc-400 font-mono">{{ opponent.progress }}% AC</span>
                      </div>
                      <div class="h-2 w-full bg-zinc-800 rounded-full overflow-hidden">
                        <div
                          class="h-full bg-rose-500 shadow-[0_0_10px_rgba(244,63,94,0.5)] transition-all duration-500 ease-out"
                          :style="{ width: opponent.progress + '%' }"
                        ></div>
                      </div>
                    </div>
                  </div>
                </div>

                <div
                  class="flex-1 bg-black/40 border border-white/5 rounded-2xl flex flex-col min-h-0 overflow-hidden"
                >
                  <div
                    class="flex items-center gap-2 px-4 py-3 border-b border-white/5 bg-white/[0.02] shrink-0"
                  >
                    <Activity class="w-3.5 h-3.5 text-zinc-400" />
                    <span class="text-xs font-bold text-zinc-400 uppercase tracking-widest"
                      >实时情报流</span
                    >
                  </div>
                  <div
                    class="flex-1 overflow-y-auto custom-scrollbar p-3 space-y-2 font-mono text-xs"
                  >
                    <TransitionGroup name="list">
                      <div
                        v-for="log in battleLogs"
                        :key="log.id"
                        class="flex gap-3 items-start p-2.5 rounded-lg bg-zinc-900/50 border border-white/5 hover:bg-zinc-800 transition-colors"
                      >
                        <div class="shrink-0 mt-0.5">
                          <FileText
                            v-if="log.type === 'CODE_UPDATE'"
                            class="w-3.5 h-3.5 text-blue-400"
                          />
                          <AlertCircle
                            v-else-if="log.type === 'TEST_FAIL'"
                            class="w-3.5 h-3.5 text-rose-500"
                          />
                          <CheckCircle2
                            v-else-if="log.type === 'TEST_PASS'"
                            class="w-3.5 h-3.5 text-yellow-500"
                          />
                          <Trophy
                            v-else-if="log.type === 'SUBMIT_AC'"
                            class="w-3.5 h-3.5 text-[#FF4C00]"
                          />
                          <Info v-else class="w-3.5 h-3.5 text-zinc-500" />
                        </div>
                        <div class="flex flex-col gap-1 min-w-0">
                          <span class="text-zinc-300 leading-snug">{{ log.message }}</span>
                          <span class="text-[9px] text-zinc-600">{{ log.time }}</span>
                        </div>
                      </div>
                    </TransitionGroup>
                  </div>
                </div>
              </div>
            </pane>
          </splitpanes>
        </pane>
      </splitpanes>
    </div>

    <JudgePanel
      :show="showJudgePanel"
      :status="judgeStatus"
      :checkpoints="checkpoints"
      mode="battle"
      @close="showJudgePanel = false"
    />

    <Transition name="fade">
      <div
        v-if="battleStatus === 'VICTORY' || battleStatus === 'DEFEAT'"
        class="absolute inset-0 z-[150] flex items-center justify-center bg-black/80 backdrop-blur-md p-6"
      >
        <div
          class="w-full max-w-md rounded-3xl border p-8 flex flex-col items-center text-center shadow-2xl relative overflow-hidden"
          :class="
            battleStatus === 'VICTORY'
              ? 'bg-[#09090b] border-[#FF4C00]/30 shadow-[#FF4C00]/20'
              : 'bg-[#09090b] border-zinc-800'
          "
        >
          <div
            v-if="battleStatus === 'VICTORY'"
            class="absolute top-0 left-1/2 -translate-x-1/2 w-64 h-64 bg-[#FF4C00]/20 blur-[80px] rounded-full pointer-events-none"
          ></div>

          <div class="relative mb-6">
            <Trophy
              v-if="battleStatus === 'VICTORY'"
              class="w-20 h-20 text-[#FF4C00] drop-shadow-[0_0_15px_#FF4C00]"
            />
            <XCircle v-else class="w-20 h-20 text-zinc-600" />
          </div>

          <h2 class="text-4xl font-black italic tracking-tighter text-white mb-2">
            {{ battleStatus === 'VICTORY' ? 'VICTORY' : 'DEFEAT' }}
          </h2>
          <p class="text-zinc-400 font-medium mb-8">
            {{ battleStatus === 'VICTORY' ? '恭喜！你击败了对手' : '很遗憾，对手抢先一步' }}
          </p>

          <div
            class="flex items-center gap-2 mb-8 px-4 py-2 rounded-full bg-white/5 border border-white/5"
          >
            <span class="text-xs text-zinc-400 font-bold uppercase tracking-wider">排位积分</span>
            <span
              class="font-mono font-bold text-lg"
              :class="battleStatus === 'VICTORY' ? 'text-emerald-400' : 'text-rose-400'"
              >{{ battleStatus === 'VICTORY' ? '+25' : '-15' }}</span
            >
          </div>

          <div class="flex flex-col gap-3 w-full">
            <div class="flex gap-3 w-full">
              <Button
                class="flex-1 bg-[#FF4C00] hover:bg-[#ff5f1f] text-white font-bold h-12 rounded-xl"
                @click="router.replace('/battle/matchmaking')"
              >
                <RotateCcw class="w-4 h-4 mr-2" /> 再次匹配
              </Button>
              <Button
                class="flex-1 bg-zinc-800 hover:bg-zinc-700 text-white font-bold h-12 rounded-xl border border-white/5"
                @click="continueResearch"
              >
                <ArrowRight class="w-4 h-4 ml-2" /> 留在房间
              </Button>
            </div>

            <Button
              v-if="battleStatus === 'DEFEAT'"
              variant="ghost"
              class="w-full text-zinc-500 hover:text-white hover:bg-white/5 h-10 rounded-xl"
              @click="viewOpponentCode"
            >
              <Eye class="w-4 h-4 mr-2" /> 查看对手代码
            </Button>
            <Button
              v-else
              variant="ghost"
              class="w-full text-zinc-500 hover:text-white hover:bg-white/5 h-10 rounded-xl"
              @click="addToFavorites"
            >
              <Star class="w-4 h-4 mr-2" /> 收藏本题
            </Button>
          </div>
        </div>
      </div>
    </Transition>

    <ArenaDialog
      v-model="showExitDialog"
      title="正在进行对局"
      confirm-text="放弃比赛"
      @confirm="confirmForceExit"
    >
      <div class="text-center space-y-4">
        <p class="text-zinc-400">
          现在离开将被<span class="text-red-500 font-bold">判负</span
          >，且扣除排位积分。<br />确认要放弃比赛吗？
        </p>
      </div>
    </ArenaDialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { useRouter, useRoute, onBeforeRouteLeave } from 'vue-router'
import { Splitpanes, Pane } from 'splitpanes'
import 'splitpanes/dist/splitpanes.css'
import {
  Timer,
  Play,
  Send,
  Swords,
  Activity,
  Loader2,
  Trophy,
  XCircle,
  AlertCircle,
  CheckCircle2,
  FileText,
  Info,
  RotateCcw,
  ArrowRight,
  Eye,
  Star,
  FlaskConical,
  Terminal,
  Plus,
  X,
  SquareTerminal,
  ChevronDown,
  ChevronUp,
  User,
  ShieldAlert,
} from 'lucide-vue-next'
import { Button } from '@/components/ui/button'
import ArenaExitButton from '@/components/arena/ArenaExitButton.vue'
import ArenaDialog from '@/components/arena/ArenaDialog.vue'
import CodeEditor from '@/components/code/CodeEditor.vue'
import JudgePanel from '@/components/code/JudgePanel.vue'
import ProblemDescription from '@/views/problem/components/ProblemDescription.vue'
import { checkRoomValidity } from '@/api/arena'

const router = useRouter()
const route = useRoute()

// --- Types ---
type BattleStatus = 'PREPARING' | 'FIGHTING' | 'VICTORY' | 'DEFEAT'
type LogType = 'INFO' | 'CODE_UPDATE' | 'TEST_FAIL' | 'TEST_PASS' | 'SUBMIT_AC'
interface BattleLog {
  id: number
  time: string
  message: string
  type: LogType
}
interface TestCase {
  inputs: Record<string, string>
}
type RunStatus = 'AC' | 'WA' | 'ERROR'
interface RunResult {
  status: RunStatus
  input: string
  output: string
  expected: string
  runtime: string
}
type CheckpointStatus = 'pending' | 'running' | 'AC' | 'WA' | 'TLE' | 'MLE' | 'RE'
interface Checkpoint {
  id: number
  status: CheckpointStatus
  time?: number
}

// --- State ---
const isVerifying = ref(true)
const battleStatus = ref<BattleStatus>('PREPARING')
const timer = ref(1800)
const myProgress = ref(0)
const battleLogs = ref<BattleLog[]>([])
const showExitDialog = ref(false)

// Layout Sizes
const maximizedPane = ref<'none' | 'left' | 'middle'>('none')
const paneSize = reactive({ left: 25, middle: 55, right: 20 })
const lastSize = reactive({ left: 25, middle: 55, right: 20 })
const isConsoleOpen = ref(true)
const consoleSizes = reactive({ editor: 70, console: 30 })
const lastConsoleSize = ref(30)

// Editor & Console
const editorCode = ref(
  `class Solution {\n    public int[] twoSum(int[] nums, int target) {\n        \n    }\n}`,
)
const isViewingOpponentCode = ref(false)
const activeConsoleTab = ref<'testcase' | 'result'>('testcase')
const testCases = ref<TestCase[]>([
  { inputs: { nums: '[2,7,11,15]', target: '9' } },
  { inputs: { nums: '[3,2,4]', target: '6' } },
])
const activeCaseIndex = ref(0)
const isRunning = ref(false)
const isSubmitting = ref(false)
const runResults = ref<RunResult[]>([])
const activeResultIndex = ref(0)

// Judge Panel State
const showJudgePanel = ref(false)
const judgeStatus = ref<'judging' | 'accepted' | 'rejected'>('judging')
const checkpoints = ref<Checkpoint[]>([])

// Opponent State
const opponent = ref({
  name: 'AlgorithmMaster_99',
  avatar: 'https://api.dicebear.com/7.x/avataaars/svg?seed=John',
  progress: 0,
  linesOfCode: 0,
})

const problem = ref({
  id: '1001',
  title: '两数之和',
  content: `<p>给定一个整数数组 <code>nums</code> 和一个整数目标值 <code>target</code>，请你在该数组中找出 <strong>和为目标值</strong> <em><code>target</code></em>  的那 <strong>两个</strong> 整数，并返回它们的数组下标。</p>`,
  examples: [
    { input: 'nums = [2,7,11,15], target = 9', output: '[0,1]' },
    { input: 'nums = [3,2,4], target = 6', output: '[1,2]' },
  ],
  tags: ['数组', '哈希表'],
})

// --- Computed ---
const formattedTime = computed(() => {
  const m = Math.floor(timer.value / 60)
    .toString()
    .padStart(2, '0')
  const s = (timer.value % 60).toString().padStart(2, '0')
  return `${m}:${s}`
})
const statusText = computed(() => {
  switch (battleStatus.value) {
    case 'PREPARING':
      return '准备就绪'
    case 'FIGHTING':
      return '对决中'
    case 'VICTORY':
      return '胜利'
    case 'DEFEAT':
      return '失败'
  }
})
const statusColor = computed(() => {
  switch (battleStatus.value) {
    case 'PREPARING':
      return 'text-zinc-500'
    case 'FIGHTING':
      return 'text-[#FF4C00] animate-pulse'
    case 'VICTORY':
      return 'text-emerald-500'
    case 'DEFEAT':
      return 'text-zinc-500'
  }
})

// --- Methods ---
const handleResize = (event: { min: number; max: number; size: number }[]) => {
  if (maximizedPane.value === 'none' && event && event.length >= 3) {
    paneSize.left = event[0].size
    paneSize.middle = event[1].size
    paneSize.right = event[2].size
  }
}

const toggleMaximizeLeft = () => {
  if (maximizedPane.value === 'left') {
    maximizedPane.value = 'none'
    paneSize.left = lastSize.left
    paneSize.middle = lastSize.middle
    paneSize.right = lastSize.right
  } else {
    if (maximizedPane.value === 'none') {
      lastSize.left = paneSize.left
      lastSize.middle = paneSize.middle
      lastSize.right = paneSize.right
    }
    maximizedPane.value = 'left'
    paneSize.left = 100 - lastSize.right
    paneSize.middle = 0
    paneSize.right = lastSize.right
  }
}

const toggleMaximizeMiddle = () => {
  if (maximizedPane.value === 'middle') {
    maximizedPane.value = 'none'
    paneSize.left = lastSize.left
    paneSize.middle = lastSize.middle
    paneSize.right = lastSize.right
  } else {
    if (maximizedPane.value === 'none') {
      lastSize.left = paneSize.left
      lastSize.middle = paneSize.middle
      lastSize.right = paneSize.right
    }
    maximizedPane.value = 'middle'
    paneSize.left = 0
    paneSize.middle = 100 - lastSize.right
    paneSize.right = lastSize.right
  }
}

const handleConsoleResize = (event: { size: number }[]) => {
  if (event && event.length >= 2) {
    consoleSizes.editor = event[0].size
    consoleSizes.console = event[1].size
    if (consoleSizes.console > 5) lastConsoleSize.value = consoleSizes.console
    else toggleConsole()
  }
}
const toggleConsole = () => {
  if (isConsoleOpen.value) {
    isConsoleOpen.value = false
    consoleSizes.editor = 100
    consoleSizes.console = 0
  } else {
    isConsoleOpen.value = true
    const targetSize = lastConsoleSize.value < 10 ? 30 : lastConsoleSize.value
    consoleSizes.editor = 100 - targetSize
    consoleSizes.console = targetSize
  }
}

const handleAddTestCase = () => {
  const newInputs: Record<string, string> = {}
  if (testCases.value.length > 0)
    Object.keys(testCases.value[0].inputs).forEach((k) => (newInputs[k] = ''))
  else newInputs['param'] = ''
  testCases.value.push({ inputs: newInputs })
  activeCaseIndex.value = testCases.value.length - 1
}

const handleRemoveTestCase = (index: number) => {
  if (testCases.value.length <= 1) return
  testCases.value.splice(index, 1)
  if (activeCaseIndex.value >= testCases.value.length)
    activeCaseIndex.value = testCases.value.length - 1
  else if (activeCaseIndex.value > index) activeCaseIndex.value--
}

const handleRunTest = () => {
  if (isRunning.value) return
  if (!isConsoleOpen.value) toggleConsole()
  activeConsoleTab.value = 'result'
  isRunning.value = true
  runResults.value = []
  activeResultIndex.value = 0

  setTimeout(() => {
    isRunning.value = false
    runResults.value = testCases.value.map((tc, idx) => {
      const isPass = Math.random() > 0.4
      if (isPass) addLog('TEST_PASS', `自测 Case ${idx + 1} 通过`)
      else addLog('TEST_FAIL', `自测 Case ${idx + 1} 失败`)

      return {
        status: isPass ? 'AC' : 'WA',
        input: Object.entries(tc.inputs)
          .map(([k, v]) => `${k}=${v}`)
          .join('\n'),
        output: isPass ? '[0,1]' : '[0,0]',
        expected: '[0,1]',
        runtime: `${Math.floor(Math.random() * 5) + 1}ms`,
      }
    })
  }, 1000)
}

const handleSubmit = async () => {
  if (isSubmitting.value || battleStatus.value !== 'FIGHTING') return

  isSubmitting.value = true
  showJudgePanel.value = true
  judgeStatus.value = 'judging'
  checkpoints.value = Array.from({ length: 15 }, (_, i) => ({ id: i + 1, status: 'pending' }))

  let hasError = false
  for (let i = 0; i < checkpoints.value.length; i++) {
    checkpoints.value[i].status = 'running'
    await new Promise((r) => setTimeout(r, 150))

    const isPass = Math.random() > 0.1
    if (isPass) {
      checkpoints.value[i].status = 'AC'
      checkpoints.value[i].time = Math.floor(Math.random() * 8) + 1
    } else {
      checkpoints.value[i].status = 'WA'
      checkpoints.value[i].time = 25
      hasError = true
    }
  }

  isSubmitting.value = false
  if (hasError) {
    judgeStatus.value = 'rejected'
    addLog('TEST_FAIL', '提交未通过：存在错误测试点')
  } else {
    judgeStatus.value = 'accepted'
    myProgress.value = 100
    addLog('SUBMIT_AC', '提交通过 (AC)！')

    // 延迟 1.5 秒后自动关闭雷达，并弹出全屏的 VICTORY 结算界面
    setTimeout(() => {
      showJudgePanel.value = false
      endGame('VICTORY')
    }, 1500)
  }
}

// --- Battle Flow ---
let gameTimer: number | null = null
let opponentTimer: number | null = null

const initializeBattle = async () => {
  isVerifying.value = false
  startBattle()
}

onMounted(() => initializeBattle())
onUnmounted(() => stopTimers())

function stopTimers() {
  if (gameTimer) clearInterval(gameTimer)
  if (opponentTimer) clearInterval(opponentTimer)
}

function startBattle() {
  battleStatus.value = 'FIGHTING'
  addLog('INFO', '对局正式开始！请开始编写代码。')

  gameTimer = window.setInterval(() => {
    if (timer.value > 0) timer.value--
    else endGame('DEFEAT')
  }, 1000)

  // 对手 AI 模拟
  opponentTimer = window.setInterval(() => {
    if (battleStatus.value !== 'FIGHTING') return
    if (Math.random() < 0.8) {
      opponent.value.progress = Math.min(
        100,
        opponent.value.progress + (Math.random() > 0.5 ? 10 : 0),
      )
      // 当对手进度达到 100% 时，强制关闭可能打开的我方雷达，并跳转到失败
      if (opponent.value.progress >= 100) {
        addLog('INFO', '对手已率先通过本题！')
        showJudgePanel.value = false
        endGame('DEFEAT')
      }
    }
  }, 3000)
}

function endGame(result: 'VICTORY' | 'DEFEAT') {
  stopTimers()
  battleStatus.value = result
}

function addLog(type: LogType, message: string) {
  const now = new Date()
  const timeStr = `${now.getHours().toString().padStart(2, '0')}:${now.getMinutes().toString().padStart(2, '0')}:${now.getSeconds().toString().padStart(2, '0')}`
  battleLogs.value.unshift({ id: Date.now(), time: timeStr, message, type })
}

// --- Route & Modals ---
onBeforeRouteLeave((to, from, next) => {
  if (battleStatus.value === 'FIGHTING') {
    showExitDialog.value = true
    next(false)
  } else next()
})

const handleExitClick = () => {
  if (battleStatus.value === 'FIGHTING') showExitDialog.value = true
  else router.replace('/arena')
}
const confirmForceExit = () => {
  battleStatus.value = 'DEFEAT'
  showExitDialog.value = false
  router.replace('/arena')
}

const getStatusText = (status: RunStatus) => (status === 'AC' ? 'Accepted' : 'Wrong Answer')
const continueResearch = () => {
  battleStatus.value = 'PREPARING'
}
const viewOpponentCode = () => {
  editorCode.value = `// Opponent's Code\n// Solved in ${1800 - timer.value}s`
  isViewingOpponentCode.value = true
  battleStatus.value = 'PREPARING'
}
const addToFavorites = () => alert('已收藏本题')
</script>

<style scoped>
.custom-scrollbar::-webkit-scrollbar {
  width: 4px;
}
.custom-scrollbar::-webkit-scrollbar-track {
  background: transparent;
}
.custom-scrollbar::-webkit-scrollbar-thumb {
  background: rgba(255, 255, 255, 0.1);
  border-radius: 4px;
}
.custom-scrollbar::-webkit-scrollbar-thumb:hover {
  background: rgba(255, 255, 255, 0.2);
}

.list-enter-active,
.list-leave-active {
  transition: all 0.3s ease;
}
.list-enter-from,
.list-leave-to {
  opacity: 0;
  transform: translateX(-10px);
}
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.5s ease;
}
.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

:deep(.splitpanes__pane) {
  overflow: hidden;
}
.no-scrollbar::-webkit-scrollbar {
  display: none;
}
.no-scrollbar {
  -ms-overflow-style: none;
  scrollbar-width: none;
}
</style>
