// Copyright (C)  Nick Fitzgerald
// License: Apache-2.0 OR MIT
//
// Copied from abandoned `console_error_panic_hook` crate

cfg_if::cfg_if! {
    if #[cfg(target_arch = "wasm32")] {
        use wasm_bindgen::prelude::wasm_bindgen;

        #[wasm_bindgen]
        extern {
            #[wasm_bindgen(js_namespace = console)]
            fn error(msg: String);

            type Error;

            #[wasm_bindgen(constructor)]
            fn new() -> Error;

            #[wasm_bindgen(structural, method, getter)]
            fn stack(error: &Error) -> String;
        }

        fn hook(info: &std::panic::PanicHookInfo) {
            let mut msg = info.to_string();
            msg.push_str("\n\nStack:\n\n");
            msg.push_str(&Error::new().stack());
            msg.push_str("\n\n");
            error(msg);
        }
    } else {
        use std::io::Write;

        fn hook(info: &std::panic::PanicHookInfo) {
            let _ = writeln!(std::io::stderr(), "{info}");
        }
    }
}

/// Set the `console.error` panic hook the first time this is called.
/// Subsequent invocations do nothing.
pub fn set_hook_once() {
    use std::sync::Once;
    static SET_HOOK: Once = Once::new();
    SET_HOOK.call_once(|| {
        std::panic::set_hook(Box::new(hook));
    });
}
