package com.kandclay.utils;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.math.Interpolation;
import de.eskalon.commons.screen.transition.impl.GLTransitionsShaderTransition;

public class Shaders {

    public static class Transitions {
        public static final String BOWTIE = "// Author: huynx\n" +
            "// License: MIT\n" +
            "\n" +
            "vec2 bottom_left = vec2(0.0, 1.0);\n" +
            "vec2 bottom_right = vec2(1.0, 1.0);\n" +
            "vec2 top_left = vec2(0.0, 0.0);\n" +
            "vec2 top_right = vec2(1.0, 0.0);\n" +
            "vec2 center = vec2(0.5, 0.5);\n" +
            "\n" +
            "float check(vec2 p1, vec2 p2, vec2 p3)\n" +
            "{\n" +
            "  return (p1.x - p3.x) * (p2.y - p3.y) - (p2.x - p3.x) * (p1.y - p3.y);\n" +
            "}\n" +
            "\n" +
            "bool PointInTriangle (vec2 pt, vec2 p1, vec2 p2, vec2 p3)\n" +
            "{\n" +
            "    bool b1, b2, b3;\n" +
            "    b1 = check(pt, p1, p2) < 0.0;\n" +
            "    b2 = check(pt, p2, p3) < 0.0;\n" +
            "    b3 = check(pt, p3, p1) < 0.0;\n" +
            "    return ((b1 == b2) && (b2 == b3));\n" +
            "}\n" +
            "\n" +
            "bool in_left_triangle(vec2 p){\n" +
            "  vec2 vertex1, vertex2, vertex3;\n" +
            "  vertex1 = vec2(progress, 0.5);\n" +
            "  vertex2 = vec2(0.0, 0.5-progress);\n" +
            "  vertex3 = vec2(0.0, 0.5+progress);\n" +
            "  if (PointInTriangle(p, vertex1, vertex2, vertex3))\n" +
            "  {\n" +
            "    return true;\n" +
            "  }\n" +
            "  return false;\n" +
            "}\n" +
            "\n" +
            "bool in_right_triangle(vec2 p){\n" +
            "  vec2 vertex1, vertex2, vertex3;\n" +
            "  vertex1 = vec2(1.0-progress, 0.5);\n" +
            "  vertex2 = vec2(1.0, 0.5-progress);\n" +
            "  vertex3 = vec2(1.0, 0.5+progress);\n" +
            "  if (PointInTriangle(p, vertex1, vertex2, vertex3))\n" +
            "  {\n" +
            "    return true;\n" +
            "  }\n" +
            "  return false;\n" +
            "}\n" +
            "\n" +
            "float blur_edge(vec2 bot1, vec2 bot2, vec2 top, vec2 testPt)\n" +
            "{\n" +
            "  vec2 lineDir = bot1 - top;\n" +
            "  vec2 perpDir = vec2(lineDir.y, -lineDir.x);\n" +
            "  vec2 dirToPt1 = bot1 - testPt;\n" +
            "  float dist1 = abs(dot(normalize(perpDir), dirToPt1));\n" +
            "  \n" +
            "  lineDir = bot2 - top;\n" +
            "  perpDir = vec2(lineDir.y, -lineDir.x);\n" +
            "  dirToPt1 = bot2 - testPt;\n" +
            "  float min_dist = min(abs(dot(normalize(perpDir), dirToPt1)), dist1);\n" +
            "  \n" +
            "  if (min_dist < 0.005) {\n" +
            "    return min_dist / 0.005;\n" +
            "  }\n" +
            "  else  {\n" +
            "    return 1.0;\n" +
            "  };\n" +
            "}\n" +
            "\n" +
            "\n" +
            "vec4 transition (vec2 uv) {\n" +
            "  if (in_left_triangle(uv))\n" +
            "  {\n" +
            "    if (progress < 0.1)\n" +
            "    {\n" +
            "      return getFromColor(uv);\n" +
            "    }\n" +
            "    if (uv.x < 0.5)\n" +
            "    {\n" +
            "      vec2 vertex1 = vec2(progress, 0.5);\n" +
            "      vec2 vertex2 = vec2(0.0, 0.5-progress);\n" +
            "      vec2 vertex3 = vec2(0.0, 0.5+progress);\n" +
            "      return mix(\n" +
            "        getFromColor(uv),\n" +
            "        getToColor(uv),\n" +
            "        blur_edge(vertex2, vertex3, vertex1, uv)\n" +
            "      );\n" +
            "    }\n" +
            "    else\n" +
            "    {\n" +
            "      if (progress > 0.0)\n" +
            "      {\n" +
            "        return getToColor(uv);\n" +
            "      }\n" +
            "      else\n" +
            "      {\n" +
            "        return getFromColor(uv);\n" +
            "      }\n" +
            "    }    \n" +
            "  }\n" +
            "  else if (in_right_triangle(uv))\n" +
            "  {\n" +
            "    if (uv.x >= 0.5)\n" +
            "    {\n" +
            "      vec2 vertex1 = vec2(1.0-progress, 0.5);\n" +
            "      vec2 vertex2 = vec2(1.0, 0.5-progress);\n" +
            "      vec2 vertex3 = vec2(1.0, 0.5+progress);\n" +
            "      return mix(\n" +
            "        getFromColor(uv),\n" +
            "        getToColor(uv),\n" +
            "        blur_edge(vertex2, vertex3, vertex1, uv)\n" +
            "      );  \n" +
            "    }\n" +
            "    else\n" +
            "    {\n" +
            "      return getFromColor(uv);\n" +
            "    }\n" +
            "  }\n" +
            "  else {\n" +
            "    return getFromColor(uv);\n" +
            "  }\n" +
            "}";

        public static final String HEART = "// Author: gre\n" +
            "// License: MIT\n" +
            "\n" +
            "float inHeart (vec2 p, vec2 center, float size) {\n" +
            "  if (size==0.0) return 0.0;\n" +
            "  vec2 o = (p-center)/(1.6*size);\n" +
            "  float a = o.x*o.x+o.y*o.y-0.3;\n" +
            "  return step(a*a*a, o.x*o.x*o.y*o.y*o.y);\n" +
            "}\n" +
            "vec4 transition (vec2 uv) {\n" +
            "  return mix(\n" +
            "    getFromColor(uv),\n" +
            "    getToColor(uv),\n" +
            "    inHeart(uv, vec2(0.5, 0.4), progress)\n" +
            "  );\n" +
            "}\n";

        public static final String WIND = "// Author: gre\n" +
            "// License: MIT\n" +
            "\n" +
            "// Custom parameters\n" +
            "uniform float size; // = 0.2\n" +
            "\n" +
            "float rand (vec2 co) {\n" +
            "  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);\n" +
            "}\n" +
            "\n" +
            "vec4 transition (vec2 uv) {\n" +
            "  float r = rand(vec2(0, uv.y));\n" +
            "  float m = smoothstep(0.0, -size, uv.x*(1.0-size) + size*r - (progress * (1.0 + size)));\n" +
            "  return mix(\n" +
            "    getFromColor(uv),\n" +
            "    getToColor(uv),\n" +
            "    m\n" +
            "  );\n" +
            "}\n";

        public static final String RAIN = "// Author: 0gust1\n" +
            "// License: MIT\n" +
            "//My own first transition — based on crosshatch code (from pthrasher), using  simplex noise formula (copied and pasted)\n" +
            "//-> cooler with high contrasted images (isolated dark subject on light background f.e.)\n" +
            "//TODO : try to rebase it on DoomTransition (from zeh)?\n" +
            "//optimizations :\n" +
            "//luminance (see http://stackoverflow.com/questions/596216/formula-to-determine-brightness-of-rgb-color#answer-596241)\n" +
            "// Y = (R+R+B+G+G+G)/6\n" +
            "//or Y = (R+R+R+B+G+G+G+G)>>3 \n" +
            "\n" +
            "\n" +
            "//direction of movement :  0 : up, 1, down\n" +
            "uniform bool direction; // = 1 \n" +
            "//luminance threshold\n" +
            "uniform float l_threshold; // = 0.8 \n" +
            "//does the movement takes effect above or below luminance threshold ?\n" +
            "uniform bool above; // = false \n" +
            "\n" +
            "\n" +
            "//Random function borrowed from everywhere\n" +
            "float rand(vec2 co){\n" +
            "  return fract(sin(dot(co.xy ,vec2(12.9898,78.233))) * 43758.5453);\n" +
            "}\n" +
            "\n" +
            "\n" +
            "// Simplex noise :\n" +
            "// Description : Array and textureless GLSL 2D simplex noise function.\n" +
            "//      Author : Ian McEwan, Ashima Arts.\n" +
            "//  Maintainer : ijm\n" +
            "//     Lastmod : 20110822 (ijm)\n" +
            "//     License : MIT  \n" +
            "//               2011 Ashima Arts. All rights reserved.\n" +
            "//               Distributed under the MIT License. See LICENSE file.\n" +
            "//               https://github.com/ashima/webgl-noise\n" +
            "// \n" +
            "\n" +
            "vec3 mod289(vec3 x) {\n" +
            "  return x - floor(x * (1.0 / 289.0)) * 289.0;\n" +
            "}\n" +
            "\n" +
            "vec2 mod289(vec2 x) {\n" +
            "  return x - floor(x * (1.0 / 289.0)) * 289.0;\n" +
            "}\n" +
            "\n" +
            "vec3 permute(vec3 x) {\n" +
            "  return mod289(((x*34.0)+1.0)*x);\n" +
            "}\n" +
            "\n" +
            "float snoise(vec2 v)\n" +
            "  {\n" +
            "  const vec4 C = vec4(0.211324865405187,  // (3.0-sqrt(3.0))/6.0\n" +
            "                      0.366025403784439,  // 0.5*(sqrt(3.0)-1.0)\n" +
            "                     -0.577350269189626,  // -1.0 + 2.0 * C.x\n" +
            "                      0.024390243902439); // 1.0 / 41.0\n" +
            "// First corner\n" +
            "  vec2 i  = floor(v + dot(v, C.yy) );\n" +
            "  vec2 x0 = v -   i + dot(i, C.xx);\n" +
            "\n" +
            "// Other corners\n" +
            "  vec2 i1;\n" +
            "  //i1.x = step( x0.y, x0.x ); // x0.x > x0.y ? 1.0 : 0.0\n" +
            "  //i1.y = 1.0 - i1.x;\n" +
            "  i1 = (x0.x > x0.y) ? vec2(1.0, 0.0) : vec2(0.0, 1.0);\n" +
            "  // x0 = x0 - 0.0 + 0.0 * C.xx ;\n" +
            "  // x1 = x0 - i1 + 1.0 * C.xx ;\n" +
            "  // x2 = x0 - 1.0 + 2.0 * C.xx ;\n" +
            "  vec4 x12 = x0.xyxy + C.xxzz;\n" +
            "  x12.xy -= i1;\n" +
            "\n" +
            "// Permutations\n" +
            "  i = mod289(i); // Avoid truncation effects in permutation\n" +
            "  vec3 p = permute( permute( i.y + vec3(0.0, i1.y, 1.0 ))\n" +
            "\t\t+ i.x + vec3(0.0, i1.x, 1.0 ));\n" +
            "\n" +
            "  vec3 m = max(0.5 - vec3(dot(x0,x0), dot(x12.xy,x12.xy), dot(x12.zw,x12.zw)), 0.0);\n" +
            "  m = m*m ;\n" +
            "  m = m*m ;\n" +
            "\n" +
            "// Gradients: 41 points uniformly over a line, mapped onto a diamond.\n" +
            "// The ring size 17*17 = 289 is close to a multiple of 41 (41*7 = 287)\n" +
            "\n" +
            "  vec3 x = 2.0 * fract(p * C.www) - 1.0;\n" +
            "  vec3 h = abs(x) - 0.5;\n" +
            "  vec3 ox = floor(x + 0.5);\n" +
            "  vec3 a0 = x - ox;\n" +
            "\n" +
            "// Normalise gradients implicitly by scaling m\n" +
            "// Approximation of: m *= inversesqrt( a0*a0 + h*h );\n" +
            "  m *= 1.79284291400159 - 0.85373472095314 * ( a0*a0 + h*h );\n" +
            "\n" +
            "// Compute final noise value at P\n" +
            "  vec3 g;\n" +
            "  g.x  = a0.x  * x0.x  + h.x  * x0.y;\n" +
            "  g.yz = a0.yz * x12.xz + h.yz * x12.yw;\n" +
            "  return 130.0 * dot(m, g);\n" +
            "}\n" +
            "\n" +
            "// Simplex noise -- end\n" +
            "\n" +
            "float luminance(vec4 color){\n" +
            "  //(0.299*R + 0.587*G + 0.114*B)\n" +
            "  return color.r*0.299+color.g*0.587+color.b*0.114;\n" +
            "}\n" +
            "\n" +
            "vec2 center = vec2(1.0, direction);\n" +
            "\n" +
            "vec4 transition(vec2 uv) {\n" +
            "  vec2 p = uv.xy / vec2(1.0).xy;\n" +
            "  if (progress == 0.0) {\n" +
            "    return getFromColor(p);\n" +
            "  } else if (progress == 1.0) {\n" +
            "    return getToColor(p);\n" +
            "  } else {\n" +
            "    float x = progress;\n" +
            "    float dist = distance(center, p)- progress*exp(snoise(vec2(p.x, 0.0)));\n" +
            "    float r = x - rand(vec2(p.x, 0.1));\n" +
            "    float m;\n" +
            "    if(above){\n" +
            "     m = dist <= r && luminance(getFromColor(p))>l_threshold ? 1.0 : (progress*progress*progress);\n" +
            "    }\n" +
            "    else{\n" +
            "     m = dist <= r && luminance(getFromColor(p))<l_threshold ? 1.0 : (progress*progress*progress);  \n" +
            "    }\n" +
            "    return mix(getFromColor(p), getToColor(p), m);    \n" +
            "  }\n" +
            "}\n";
    }

    public static GLTransitionsShaderTransition createTransition(String string, float duration, Interpolation interpolation) {
        float progress = 0;
        GLTransitionsShaderTransition transition = new GLTransitionsShaderTransition(string, duration, interpolation);

        transition.getProgram().bind();
        transition.getProgram().setUniformf("progress", progress);
        return transition;
    }

    public static GLTransitionsShaderTransition createTransition() {
        float progress = 0;
        GLTransitionsShaderTransition transition = new GLTransitionsShaderTransition(Transitions.HEART, 1f, getRandomInterpolation());

        transition.getProgram().bind();
        transition.getProgram().setUniformf("progress", progress);
        return transition;
    }

    private static Interpolation getRandomInterpolation() {
        Interpolation[] interpolations = new Interpolation[] {
            Interpolation.circle,
            Interpolation.exp10,
            Interpolation.exp5,
            Interpolation.fade,
            Interpolation.linear,
            Interpolation.pow2,
            Interpolation.pow3,
            Interpolation.pow4,
            Interpolation.pow5,
            Interpolation.sine,
        };
        Interpolation interpolation = interpolations[(int) (Math.random() * interpolations.length)];
        Gdx.app.log("Shaders", "Using interpolation: " + interpolation.getClass());
        return interpolation;
    }
}
