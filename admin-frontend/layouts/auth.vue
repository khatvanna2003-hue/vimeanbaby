<template>
  <div class="grid min-h-screen bg-surface lg:grid-cols-2">
    <aside class="relative hidden overflow-hidden bg-brand p-12 text-white lg:flex lg:flex-col">
      <div class="absolute -right-24 -top-24 h-80 w-80 rounded-full bg-white/10" aria-hidden="true" />
      <div class="absolute -bottom-32 -left-16 h-96 w-96 rounded-full bg-white/10" aria-hidden="true" />
      <img src="/logo-white.svg" alt="Vimean Baby" class="relative h-11 w-auto self-start" width="183" height="44">
      <div class="relative mt-auto max-w-md">
        <slot name="aside" />
      </div>
    </aside>
    <main class="flex items-center justify-center px-4 py-10 sm:px-8">
      <slot />
    </main>
  </div>
</template>
