<template id="richnlg">
    <div>
        <div>
            <div style="width:100%; text-align:center;">
                <h5 class="card-header">{{name}}</h5>
                <p style="font-size:small;">This demo shows how the Rich NLU system works.</p>
            </div>
            <div class="row">
                <div class="column left" style="background-color:#aaa;">
                    Placeholder for ledt sidebar
                </div>
                <div class="column middle">
                    <input v-model="msg" @keyup.enter="updateMsg" style="width:80%">

                    <h5 class="card-header">Words</h5>
                    <table class="card-body">
                        <tr v-for="word in result.wordSpans" style="text-align:justify;">
                        <td  style="color:black; background-color:lightgray">
                            {{word.start}} &mdash; {{word.label}} &mdash; {{word.end}}
                        </td>
                        </tr>
                    </table>
                    <h5 class="card-header">Parts of speech</h5>
                    <ul class="card-body">
                        <li v-for="word in result.preTerminals" style="color:red; ; background-color:pink">
                            {{word.start}}-{{word.label}}-{{word.end}}
                        </li>
                    </ul>
                    <h5 class="card-header">Phrases</h5>
                    <ul class="card-body">
                        <li v-for="word in result.nonTerminals" style="color:blue;  background-color:lightblue">
                            {{word.start}}-{{word.label}}-{{word.end}}
                        </li>
                    </ul>
                </div>
                <div class="column right" style="background-color:#aaa;">
                    Placeholder for right sidebar
                </div>
            </div>

        </div>
</template>

<script>
var p = Vue.component("richnlg", {
  name: "post-request",
  template: "#richnlg",
  data: () => ({
           msg:"I want an umbrella",
           name: "Rich NLU",
           result: ""
        }),
  created() {
    const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ msg:this.msg,id:123 })
    };
     fetch("/msg", requestOptions)
      .then(response => response.json())
      .then(data => (this.msg = data.msg));

        fetch("/parse", requestOptions)
        .then(response => response.json())
        .then(data => (this.result = data));
  },

  methods: {
    parse: function() {
        const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ msg:this.msg,id:123 })
    };
      fetch("/parse", requestOptions)
      .then(response => response.json())
      .then(data => (this.result = data));

    },
    updateMsg: function() {
      const requestOptions = {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({ msg:this.msg,id:123 })
    };
    fetch("/msg", requestOptions)
      .then(response => response.json())
      .then(data => (this.msg = data.msg));
   fetch("/parse", requestOptions)
      .then(response => response.json())
      .then(data => (this.result = data));


        }

  },

  });


</script>
<style>
* {
  box-sizing: border-box;
}

/* Create three unequal columns that floats next to each other */
.column {
  float: left;
  padding: 10px;
}

.left, .right {
  width: 20%;
}

.middle {
  width: 60%;
}

/* Clear floats after the columns */
.row:after {
  content: "";
  display: table;
  clear: both;
}

table {
  border: 1px solid black;
  border-collapse: collapse;
}

</style>
