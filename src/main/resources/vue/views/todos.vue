<template id="todos">
    <div class="card text-center m-3">
        <h5 class="card-header">Simple POST Request</h5>
        <div class="card-body">Returned Id: {{post.id}} </div>
        <div class="card-body">Returned Message: {{post.msg}} </div>
    </div>
</template>

<script>
var p = Vue.component("todos", {
  name: "post-request",
  template: "#todos",
  data: () => ({
           post: {}
        }),
  created() {
    const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        id: 103,
        msg: "The Beloved" })
    };
    fetch("/msgs", requestOptions)
      .then(response => response.json())
      .then(data => (this.post = data));
  },
  });
</script>
