declare module 'splitpanes' {
  import type { DefineComponent } from 'vue'

  export const Splitpanes: DefineComponent<
    {
      horizontal?: boolean
      pushOtherPanes?: boolean
      dblClickSplitter?: boolean
      rtl?: boolean
      firstSplitter?: boolean
      class?: string
      style?: string | object
    },
    any,
    any
  >

  export const Pane: DefineComponent<
    {
      size?: number | string
      minSize?: number | string
      maxSize?: number | string
      class?: string
      style?: string | object
    },
    any,
    any
  >
}
